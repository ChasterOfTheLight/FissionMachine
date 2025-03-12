import pybroker
from pybroker import Strategy, StrategyConfig, ExecContext
from pybroker.ext.data import AKShare
import matplotlib.pyplot as plt

# 1. 创建策略配置
config = StrategyConfig(initial_cash=500_000)

# 2. 创建数据源
strategy = Strategy(
    data_source=AKShare(),
    start_date='20241201',
    end_date='20250311',
    config=config
)

# 3. 创建策略
def buy_low(ctx: ExecContext):
    # 如果当前已经持有仓位，则不再买入。
    if ctx.long_pos():
        return
    # 如果当前的收盘价小于前一天的最低价，则下单买入。
    if ctx.bars >= 2 and ctx.close[-1] < ctx.low[-2]:
        # 计算买入的股票数量，该数量为当前资金的 25%。
        ctx.buy_shares = ctx.calc_target_shares(0.25)
        # 设置买入的限价，该限价为当前收盘价减去 0.01。
        ctx.buy_limit_price = ctx.close[-1] - 0.01
        # 设置持有仓位的时间，该时间为 3 个交易日。
        ctx.hold_bars = 3

# 4. 运行策略
strategy.add_execution(fn=buy_low, symbols=['002480'])
result = strategy.backtest()
# 查看持仓
print(result.orders)
# 查看投资组合
print(result.portfolio)
# 查看交易
print(result.trades)

# 5. 收益率绘制图形
chart = plt.subplot2grid((3, 2), (0, 0), rowspan=3, colspan=2)
chart.plot(result.portfolio.index, result.portfolio['market_value'])