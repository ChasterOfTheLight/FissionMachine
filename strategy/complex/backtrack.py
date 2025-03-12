import pybroker
from pybroker import Strategy, StrategyConfig, ExecContext, indicator, BarData
from pybroker.ext.data import AKShare
import matplotlib.pyplot as plt

# 启用数据源缓存
pybroker.enable_data_source_cache('my_strategy')

# 定义并注册指标
def volume_indicator(data: BarData):
    return data.volume

def avg_volume_10d_indicator(data: BarData):
    return data.volume.rolling(window=10).mean()

def ma5_indicator(data: BarData):
    return data.close.rolling(window=5).mean()

indicator('volume', volume_indicator)
indicator('avg_volume_10d', avg_volume_10d_indicator)
indicator('ma5', ma5_indicator)

# 1. 创建策略配置
config = StrategyConfig(initial_cash=200_000)

# 2. 创建数据源
strategy = Strategy(
    data_source=AKShare(),
    start_date='20240101',
    end_date='20240911',
    config=config
)

# 3. 创建策略
def buy_low(ctx: ExecContext):
    # 至少需要20天的数据。
    if ctx.bars < 20:
        return
    
    # 连续3个交易日放量，每天量都大于前10天的平均量, 买入。否则卖出
    volume = ctx.indicator('volume')
    avg_volume_10d = ctx.indicator('avg_volume_10d')

    # 收盘价多于5日线，加仓；低于5日线，减仓。
    ma5 = ctx.indicator('ma5')
    close = ctx.indicator('close')
    
    # 连续上涨天数小于等于3天，买入。连续跌幅大于等于2天，卖出
    close_3d = close[-3:]

    # 买入条件需所有条件均满足才行，卖出条件只有有一条符合就卖出
    if (volume[-1] > avg_volume_10d[-1] and
        volume[-2] > avg_volume_10d[-2] and
        volume[-3] > avg_volume_10d[-3] and
        close[-1] > ma5[-1] and
        close_3d.count(True) <= 3 and
        close_3d.count(False) >= 2):
        ctx.buy(ctx.symbol, 100)
    elif (volume[-1] < avg_volume_10d[-1] or
          volume[-2] < avg_volume_10d[-2] or
          volume[-3] < avg_volume_10d[-3] or
          close[-1] < ma5[-1] or
          close_3d.count(True) >= 2 or
          close_3d.count(False) <= 3):
        ctx.sell(ctx.symbol, 100)

# 4. 运行策略
strategy.add_execution(
    fn=buy_low, 
    symbols=['002480'], 
    indicators=['close', 'volume', 'ma5', 'avg_volume_10d'])

result = strategy.backtest()
# 查看投资组合
print(result.portfolio)
# 查看交易
print(result.trades)

# 5. 收益率绘制图形
chart = plt.subplot2grid((3, 2), (0, 0), rowspan=3, colspan=2)
chart.plot(result.portfolio.index, result.portfolio['market_value'])
chart.set_xlabel('date')
chart.set_ylabel('total market value')
chart.set_title('Backtest Result')
plt.show()