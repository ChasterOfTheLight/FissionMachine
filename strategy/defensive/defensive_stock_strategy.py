import akshare as ak
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import logging
import traceback

logging.basicConfig(level=logging.INFO,
                   format='%(asctime)s - %(message)s',
                   datefmt='%Y-%m-%d %H:%M:%S')

class DefensiveStockStrategy:
    def __init__(self):
        self.defensive_sectors = [
            "食品饮料", "有色金属", "公用事业", "银行", "电力行业", "保险"
        ]
        # 在初始化时获取上证指数数据
        self.index_data = ak.stock_zh_index_daily_em(symbol="sh000001")
        self.index_returns = self.index_data['close'].pct_change()
        
    def get_stock_data(self, stock_code: str, days: int = 60) -> pd.DataFrame:
        """获取个股历史数据"""
        try:
            df = ak.stock_zh_a_hist(symbol=stock_code, 
                                  period="daily",
                                  end_date=datetime.now().strftime('%Y%m%d'),
                                  adjust="qfq")
            return df.tail(days)
        except Exception as e:
            logging.error(f"获取股票{stock_code}数据失败: {e}")
            return pd.DataFrame()

    def calculate_risk_metrics(self, stock_data: pd.DataFrame) -> dict:
        """计算风险指标"""
        try:
            # 计算波动率
            returns = stock_data['收盘'].pct_change()
            volatility = returns.std() * np.sqrt(252)  # 年化波动率
            
            # 计算最大回撤
            cummax = stock_data['收盘'].cummax()
            drawdown = (stock_data['收盘'] - cummax) / cummax
            max_drawdown = drawdown.min()
            
            # 夏普比率 (假设无风险利率3%)
            risk_free_rate = 0.03
            excess_returns = returns.mean() * 252 - risk_free_rate
            sharpe_ratio = excess_returns / volatility if volatility != 0 else 0
            
            # beta值（相对于上证指数）
            beta = returns.cov(self.index_returns) / self.index_returns.var()
            
            return {
                'volatility': volatility,
                'max_drawdown': max_drawdown,
                'sharpe_ratio': sharpe_ratio,
                'beta': beta
            }
        except Exception as e:
            logging.error(f"计算风险指标失败: {e}")
            return {}

    def get_fundamental_indicators(self, stock_code: str) -> dict:
        """获取基本面指标"""
        try:
            logging.debug(f"正在获取股票 {stock_code} 的基本面指标...")
            
            # 获取市场指标数据
            market_indicator = ak.stock_a_indicator_lg(symbol=stock_code)
            logging.debug(f"市场指标数据大小: {len(market_indicator)}")
            
            if market_indicator.empty:
                logging.warning(f"股票{stock_code}未获取到市场指标数据")
                return {}
            
            latest_market = market_indicator.iloc[-1]
            # 检查市场指标
            if not all(key in latest_market for key in ['pe', 'pb', 'dv_ratio']):
                logging.warning(f"股票{stock_code}缺少必要的市场指标数据")
                return {}

            logging.debug(f"市场指标: PE={latest_market.get('pe', 'N/A')}, PB={latest_market.get('pb', 'N/A')}")
            
            # 使用同花顺接口获取财务数据
            try:
                financial_indicator = ak.stock_financial_abstract_ths(symbol=stock_code)
                if financial_indicator.empty:
                    logging.warning(f"股票{stock_code}未获取到财务数据")
                    return {}
                    
                # 提取最新一期的数据
                latest_finance = financial_indicator.iloc[-1] # 取最新数据
                logging.debug(f"最新财务数据: {latest_finance}")
                
                try:
                    # 处理ROE (净资产收益率)
                    roe_df = latest_finance['净资产收益率']
                    roe = float(roe_df.strip('%'))  # 去除百分号并转为float

                    # 处理资产负债率
                    debt_ratio_df = latest_finance['资产负债率']
                    debt_ratio = float(debt_ratio_df.strip('%'))  # 去除百分号并转为float
                    
                    return {
                        'pe_ratio': float(latest_market['pe']),
                        'pb_ratio': float(latest_market['pb']),
                        'dividend_yield': float(latest_market['dv_ratio']),
                        'roe': roe,
                        'debt_ratio': debt_ratio
                    }
                except (ValueError, TypeError) as e:
                    logging.error(f"转换财务数据时出错: {str(e)}")
                    logging.error(f"ROE原始值: {roe_df.iloc[0, -1] if not roe_df.empty else 'N/A'}, "
                                f"资产负债率原始值: {debt_ratio_df.iloc[0, -1] if not debt_ratio_df.empty else 'N/A'}")
                    return {}
                    
            except Exception as e:
                logging.error(f"获取财务数据失败: {str(e)}")
                logging.error(traceback.format_exc())
                return {}
                
        except Exception as e:
            logging.error(f"获取股票{stock_code}的基本面指标时发生错误")
            logging.error("错误详情:")
            logging.error(traceback.format_exc())
            return {}

    def defensive_stock_filter(self, stock_code: str, stock_name: str) -> dict:
        """防御性选股策略"""
        try:
            logging.info(f"\n正在分析股票: {stock_code} ({stock_name})")
            
            # 获取股票数据
            stock_data = self.get_stock_data(stock_code)
            if stock_data.empty:
                logging.warning(f"未获取到股票{stock_code}的交易数据")
                return {}
            
            # 计算风险指标
            risk_metrics = self.calculate_risk_metrics(stock_data)
            if risk_metrics:
                logging.debug(f"风险指标: 波动率={risk_metrics['volatility']:.2%}, Beta={risk_metrics['beta']:.2f}")
            
            # 获取基本面指标
            fundamental = self.get_fundamental_indicators(stock_code)
            if fundamental:
                logging.debug(f"基本面指标: PE={fundamental.get('pe_ratio', 'N/A')}, ROE={fundamental.get('roe', 'N/A')}%")
            
            # 评分过程
            score = 0
            logging.debug("开始评分...")
            
            if risk_metrics['volatility'] < 0.3:
                score += 30
                logging.debug("低波动性得分: +30")
                
            if risk_metrics['beta'] < 0.8:
                score += 20
                logging.debug("Beta得分: +20")
                
            if fundamental['dividend_yield'] > 3:
                score += 20
                logging.debug("股息率得分: +20")
                
            if (fundamental['debt_ratio'] < 60 and 
                fundamental['roe'] > 8 and 
                fundamental['pe_ratio'] < 20):
                score += 30
                logging.debug("财务稳健性得分: +30")
            
            logging.info(f"最终得分: {score}")
            
            if score >= 70:  # 只返回得分70分以上的股票
                return {
                    '股票代码': stock_code,
                    '股票名称': stock_name,
                    '评分': score,
                    '年化波动率': f"{risk_metrics['volatility']*100:.2f}%",
                    'Beta': f"{risk_metrics['beta']:.2f}",
                    '股息率': f"{fundamental['dividend_yield']:.2f}%",
                    'ROE': f"{fundamental['roe']:.2f}%",
                    'PE': f"{fundamental['pe_ratio']:.2f}",
                    '最大回撤': f"{risk_metrics['max_drawdown']*100:.2f}%"
                }
            return {}
            
        except Exception as e:
            logging.error(f"处理股票{stock_code}时发生错误: {e}")
            return {}

    def run_strategy(self):
        """运行防御性选股策略"""
        start_time = datetime.now()
        logging.info(f"\n开始执行防御性选股策略... 开始时间: {start_time.strftime('%Y-%m-%d %H:%M:%S')}")
        logging.info(f"分析行业: {', '.join(self.defensive_sectors)}")
        
        recommendations = []
        processed_stocks = 0
        
        for sector in self.defensive_sectors:
            try:
                logging.info(f"\n=== 开始分析 {sector} 行业 ===")
                stocks = ak.stock_board_industry_cons_em(symbol=sector)
                logging.info(f"获取到 {len(stocks)} 只股票")
                
                for _, stock in stocks.iterrows():
                    processed_stocks += 1
                    result = self.defensive_stock_filter(stock['代码'], stock['名称'])
                    if result:
                        recommendations.append(result)
                        logging.info(f"*** 符合条件的股票: {stock['名称']} ***")
                    
                    time.sleep(1)
                    
            except Exception as e:
                logging.error(f"处理{sector}行业时发生错误: {e}")
                logging.error("错误详情:")
                logging.error(traceback.format_exc())
                continue
        
        end_time = datetime.now()
        duration = end_time - start_time
        
        logging.info(f"\n策略执行完成")
        logging.info(f"总耗时: {duration}")
        logging.info(f"分析股票数: {processed_stocks}")
        logging.info(f"筛选出的股票数: {len(recommendations)}")
        
        # 结果排序和展示
        if recommendations:
            df_results = pd.DataFrame(recommendations)
            df_results = df_results.sort_values('评分', ascending=False)
            
            logging.info("\n" + "="*80)
            logging.info("防御性股票推荐:")
            logging.info("="*80)
            logging.info("\n" + str(df_results))
            logging.info("\n选股说明:")
            logging.info("1. 筛选低波动、高股息的防御性行业股票")
            logging.info("2. 评分标准: 低波动性(30分) + Beta值(20分) + 股息率(20分) + 财务稳健性(30分)")
            logging.info("3. 推荐股票均为总分70分以上的标的")
        else:
            logging.info("未找到符合条件的防御性股票")

if __name__ == "__main__":
    import time
    strategy = DefensiveStockStrategy()
    strategy.run_strategy()