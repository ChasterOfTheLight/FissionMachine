import os
import sys
from stock_selector import StockSelector
from pybroker import Strategy, StrategyConfig, ExecContext
from pybroker.ext.data import AKShare
import talib as ta
import pybroker
import traceback

def run_selection_and_backtest(
    start_date='2022-01-01',
    end_date='2022-12-31',
    backtest_start='1/1/2023',
    backtest_end='6/30/2023',
    stock_pool='hs300',
    top_n=20,
    use_fundamental=True,
    fallback_to_tech_only=True
):
    """
    运行选股程序并将结果传递给回测策略
    
    参数:
        start_date: 选股开始日期，格式 'YYYY-MM-DD'
        end_date: 选股结束日期，格式 'YYYY-MM-DD'
        backtest_start: 回测开始日期，格式 'M/D/YYYY'
        backtest_end: 回测结束日期，格式 'M/D/YYYY'
        stock_pool: 股票池，可选 'hs300', 'zz500', 'all_a'
        top_n: 选择排名前多少的股票
        use_fundamental: 是否使用基本面数据进行选股
        fallback_to_tech_only: 如果基本面数据获取失败，是否回退到仅使用技术指标
    """
    print("="*50)
    print(f"第一步：运行选股程序，选择{top_n}只股票")
    print(f"使用基本面数据: {'是' if use_fundamental else '否'}")
    print(f"基本面数据获取失败时回退到技术指标: {'是' if fallback_to_tech_only else '否'}")
    print("="*50)
    
    # 运行选股程序
    try:
        selector = StockSelector(
            start_date=start_date,
            end_date=end_date,
            stock_pool=stock_pool,
            top_n=top_n,
            use_fundamental=use_fundamental
        )
        
        # 获取选出的股票
        selected_stocks = selector.select_stocks()
        
        # 如果使用基本面数据失败，尝试回退到仅使用技术指标
        if not selected_stocks and use_fundamental and fallback_to_tech_only:
            print("\n基本面数据选股失败，回退到仅使用技术指标...")
            selector = StockSelector(
                start_date=start_date,
                end_date=end_date,
                stock_pool=stock_pool,
                top_n=top_n,
                use_fundamental=False
            )
            selected_stocks = selector.select_stocks()
    except Exception as e:
        print(f"选股过程发生错误: {e}")
        print(traceback.format_exc())
        return None
    
    if not selected_stocks:
        print("选股失败，没有选出任何股票")
        return None
    
    print("\n选出的股票:")
    for i, stock in enumerate(selected_stocks, 1):
        print(f"{i}. {stock}")
    
    print("\n"+"="*50)
    print(f"第二步：使用选出的{len(selected_stocks)}只股票进行回测")
    print("="*50)
    
    try:
        # 创建AKShare数据源
        akshare = AKShare()
        
        # 使用 TA-Lib 定义一个 20 天的 ROC 指标
        roc_20 = pybroker.indicator('roc_20',
                                    lambda data: ta.ROC(data.close, timeperiod=20))
        
        # 创建策略配置
        max_positions = min(10, len(selected_stocks))
        if max_positions == 0:
            print("没有足够的股票进行回测")
            return None
            
        config = StrategyConfig(initial_cash=200_000, max_long_positions=max_positions)
        pybroker.param('target_size', 1 / config.max_long_positions)
        
        rank_threshold = min(5, len(selected_stocks))
        if rank_threshold == 0:
            rank_threshold = 1
        pybroker.param('rank_threshold', rank_threshold)
        
        # 排名函数
        def rank(ctxs: dict[str, ExecContext]):
            if not ctxs:
                print("警告: 没有可用的执行上下文")
                pybroker.param('top_symbols', [])
                return
                
            try:
                scores = {
                    symbol: ctx.indicator('roc_20')[-1]
                    for symbol, ctx in ctxs.items()
                    if ctx.indicator('roc_20') is not None and len(ctx.indicator('roc_20')) > 0
                }
                
                if not scores:
                    print("警告: 没有可用的ROC得分")
                    pybroker.param('top_symbols', list(ctxs.keys())[:rank_threshold])
                    return
                    
                sorted_scores = sorted(scores.items(),
                                    key=lambda score: score[1],
                                    reverse=True)
                threshold = pybroker.param('rank_threshold')
                top_scores = sorted_scores[:threshold]
                top_symbols = [score[0] for score in top_scores]
                pybroker.param('top_symbols', top_symbols)
                
                # 打印所有股票的ROC排名
                print("\n所有股票的ROC排名:")
                for i, (symbol, score) in enumerate(sorted_scores, 1):
                    status = "✓" if symbol in top_symbols else " "
                    print(f"{i}. [{status}] {symbol}: ROC={score:.2f}%")
                print(f"\n选中的前{threshold}只股票: {', '.join(top_symbols)}")
            except Exception as e:
                print(f"排名过程发生错误: {e}")
                # 如果排名失败，使用所有股票
                pybroker.param('top_symbols', list(ctxs.keys())[:rank_threshold])
        
        # 轮动交易函数
        def rotate(ctx: ExecContext):
            try:
                if ctx.long_pos():
                    if ctx.symbol not in pybroker.param('top_symbols'):
                        ctx.sell_all_shares()
                else:
                    if ctx.symbol in pybroker.param('top_symbols'):
                        target_size = pybroker.param('target_size')
                        ctx.buy_shares = ctx.calc_target_shares(target_size)
                        if ctx.indicator('roc_20') is not None and len(ctx.indicator('roc_20')) > 0:
                            ctx.score = ctx.indicator('roc_20')[-1]
            except Exception as e:
                print(f"交易执行发生错误: {e}")
        
        # 创建策略
        strategy = Strategy(akshare,
                            start_date=backtest_start,
                            end_date=backtest_end,
                            config=config)
        
        # 设置 before_exec
        strategy.set_before_exec(rank)
        
        # 添加执行 - 使用选出的股票池
        strategy.add_execution(rotate, selected_stocks, indicators=roc_20)
        
        # 运行回测
        result = strategy.backtest(warmup=20)
        
        # 打印回测结果
        print("\n"+"="*50)
        print("回测结果")
        print("="*50)
        
        # 打印订单
        print("\n订单信息:")
        print(result.orders)
        
        # 打印投资组合信息
        print("\n投资组合信息:")
        print(result.portfolio)
        
        # 打印回测统计信息
        stats = result.stats
        print("\n回测统计:")
        print(f"总收益率: {stats.total_return:.2f}%")
        print(f"年化收益率: {stats.annual_return:.2f}%")
        print(f"夏普比率: {stats.sharpe:.2f}")
        print(f"最大回撤: {stats.max_drawdown:.2f}%")
        print(f"胜率: {stats.win_rate:.2f}%")
        
        return result
    except Exception as e:
        print(f"回测过程发生错误: {e}")
        print(traceback.format_exc())
        return None

if __name__ == "__main__":
    # 默认参数
    params = {
        'start_date': '2022-01-01',
        'end_date': '2022-12-31',
        'backtest_start': '1/1/2023',
        'backtest_end': '6/30/2023',
        'stock_pool': 'hs300',
        'top_n': 20,
        'use_fundamental': True,
        'fallback_to_tech_only': True
    }
    
    # 解析命令行参数
    if len(sys.argv) > 1:
        for arg in sys.argv[1:]:
            if '=' in arg:
                key, value = arg.split('=')
                if key in params:
                    # 转换数值类型
                    if key == 'top_n':
                        params[key] = int(value)
                    elif key in ['use_fundamental', 'fallback_to_tech_only']:
                        params[key] = value.lower() == 'true'
                    else:
                        params[key] = value
    
    # 运行选股和回测
    try:
        result = run_selection_and_backtest(**params)
        if result is None:
            print("\n回测失败，请检查日志以获取更多信息。")
            sys.exit(1)
    except Exception as e:
        print(f"\n程序执行过程中发生错误: {e}")
        print(traceback.format_exc())
        sys.exit(1) 