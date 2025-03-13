import pybroker
import pandas as pd
from pybroker import Strategy, StrategyConfig, highv, ExecContext
from pybroker.ext.data import AKShare
import numpy as np
from numba import njit
from sklearn.linear_model import LinearRegression
from sklearn.metrics import r2_score
import talib as ta

# Create an instance of AKShare.
akshare = AKShare()

# # 创建策略配置，设置 return_signals=True 以获取源 bar 数据
# config = StrategyConfig(initial_cash=200_000, return_signals=True)

# # 使用配置创建策略
# strategy = Strategy(akshare, '3/1/2023', '3/1/2024', config)

# # Calculate the CMMA indicator.
# def cmma(bar_data, lookback):

#     @njit  # Enable Numba JIT.
#     def vec_cmma(values):
#         # Initialize the result array.
#         n = len(values)
#         out = np.array([np.nan for _ in range(n)])

#         # For all bars starting at lookback:
#         for i in range(lookback, n):
#             # Calculate the moving average for the lookback.
#             ma = 0
#             for j in range(i - lookback, i):
#                 ma += values[j]
#             ma /= lookback
#             # Subtract the moving average from value.
#             out[i] = values[i] - ma
#         return out

#     # Calculate with close prices.
#     return vec_cmma(bar_data.close)

# # Register the indicator.
# cmma_20 = pybroker.indicator('cmma_20', cmma, lookback=20)

# # A strategy 当 20 日 CMMA 小于 0 时进行多头建仓 — 即当最近收盘价跌破 20 日移动平均线时.
# def buy_cmma_cross(ctx):
#     if ctx.long_pos():
#         return
#     # Place a buy order if the most recent value of the 20 day CMMA is < 0:
#     if ctx.indicator('cmma_20')[-1] < 0:
#         ctx.buy_shares = ctx.calc_target_shares(1)
#         ctx.hold_bars = 3

# # hhv
# def hhv(bar_data, period):
#     return highv(bar_data.high, period)

# # 注册 hhv_5 指标
# hhv_5 = pybroker.indicator('hhv_5', hhv, period=5)

# # 在策略中使用 hhv_5 指标
# if result.signals and '000001.SZ' in result.signals:
#     # 获取股票数据
#     df = result.signals['000001.SZ']

#     # 计算 5 日最高价
#     highest_5d = np.array([np.nan] * len(df))
#     for i in range(4, len(df)):
#         highest_5d[i] = df['high'].iloc[i-4:i+1].max()

#     # 添加到 DataFrame 中
#     df['hhv_5'] = highest_5d

# # 训练模型
# def train_slr(symbol, train_data, test_data):
#     # Train
#     # Previous day close prices.
#     train_prev_close = train_data['close'].shift(1)
#     # Calculate daily returns.
#     train_daily_returns = (train_data['close'] - train_prev_close) / train_prev_close
#     # Predict next day's return.
#     train_data['pred'] = train_daily_returns.shift(-1)
#     train_data = train_data.dropna()
#     # Train the LinearRegession model to predict the next day's return
#     # given the 20-day CMMA.
#     X_train = train_data[['cmma_20']]
#     y_train = train_data[['pred']]
#     model = LinearRegression()
#     model.fit(X_train, y_train)

#     # Test
#     test_prev_close = test_data['close'].shift(1)
#     test_daily_returns = (test_data['close'] - test_prev_close) / test_prev_close
#     test_data['pred'] = test_daily_returns.shift(-1)
#     test_data = test_data.dropna()
#     X_test = test_data[['cmma_20']]
#     y_test = test_data[['pred']]
#     # Make predictions from test data.
#     y_pred = model.predict(X_test)
#     # Print goodness of fit.
#     r2 = r2_score(y_test, np.squeeze(y_pred))
#     print(symbol, f'R^2={r2}')

#     # Return the trained model and columns to use as input data.
#     return model, ['cmma_20']

# # register model
# model_slr = pybroker.model('slr', train_slr, indicators=[cmma_20])

# def hold_long(ctx):
#     pos = ctx.long_pos()
#     if not pos:
#         # Buy if the next bar is predicted to have a positive return:
#         if ctx.preds('slr')[-1] > 0:
#             ctx.buy_shares = 100
#             # 设置止损
#             ctx.stop_loss_pct = 20
#             # 设置跟踪止盈
#             ctx.stop_trailing_pct = 20
#             ctx.stop_trailing_limit = ctx.close[-1] + 1
#             # 设置止盈
#             ctx.stop_profit_pct = 10
#             ctx.stop_profit_limit = ctx.close[-1] - 1
#     elif pos.bars > 60:
#         ctx.cancel_stops(ctx.symbol)
#     else:
#         # Sell if the next bar is predicted to have a negative return:
#         if ctx.preds('slr')[-1] < 0:
#             ctx.sell_shares = 100

# strategy.clear_executions()

# # Add the execution.
# strategy.add_execution(hold_long, '600418.SH', models=model_slr)

# result = strategy.walkforward(
#     warmup=20,
#     windows=3,
#     train_size=0.5,
#     lookahead=1,
#     calc_bootstrap=True
# )

# 使用 TA-Lib 定义一个 20 天的 ROC 指标
roc_20 = pybroker.indicator('roc_20',
                            lambda data: ta.ROC(data.close, timeperiod=20))

config = StrategyConfig(initial_cash=200_000, max_long_positions=2)
pybroker.param('target_size', 1 / config.max_long_positions)
pybroker.param('rank_threshold', 5)


# 排名
def rank(ctxs: dict[str, ExecContext]):
    scores = {
        symbol: ctx.indicator('roc_20')[-1]
        for symbol, ctx in ctxs.items()
    }
    sorted_scores = sorted(scores.items(),
                           key=lambda score: score[1],
                           reverse=True)
    threshold = pybroker.param('rank_threshold')
    top_scores = sorted_scores[:threshold]
    top_symbols = [score[0] for score in top_scores]
    pybroker.param('top_symbols', top_symbols)


# 轮动交易
def rotate(ctx: ExecContext):
    if ctx.long_pos():
        if ctx.symbol not in pybroker.param('top_symbols'):
            ctx.sell_all_shares()
    else:
        target_size = pybroker.param('target_size')
        ctx.buy_shares = ctx.calc_target_shares(target_size)
        ctx.score = ctx.indicator('roc_20')[-1]

# 创建策略
strategy = Strategy(akshare,
                    start_date='1/7/2024',
                    end_date='1/1/2025',
                    config=config)

# 设置 before_exec
strategy.set_before_exec(rank)

# 添加执行 股票池
strategy.add_execution(rotate, [
    '000001.SZ',
    '600418.SH',
    '600519.SH',
    '600536.SH',
    '600601.SH',
    '002230.SZ',
    '000028.SZ',
    '000029.SZ',
    '000030.SZ',
    '600016.SH'
], indicators=roc_20)
result = strategy.backtest(warmup=20)

# 获取结果
print(result.orders)
print(result.portfolio)