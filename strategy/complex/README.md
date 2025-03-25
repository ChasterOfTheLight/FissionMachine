# 多因子选股与回测系统

这是一个基于多因子模型的股票选择和回测系统，可以帮助您从指定的股票池中选出潜在的优质股票，并对这些股票进行回测分析。

## 功能特点

1. **多因子选股模型**：
   - 动量因子：捕捉价格趋势
   - 波动率因子：评估风险水平
   - 技术指标因子：RSI、MACD、布林带等
   - 成交量因子：分析交易活跃度
   - 趋势因子：价格与均线关系
   - 反转因子：捕捉超跌反弹机会
   - 基本面因子：市盈率、市净率、市值、总资产、净利润等
   - ESG因子：环境、社会和公司治理评分

2. **灵活的股票池选择**：
   - 沪深300成分股
   - 中证500成分股
   - 全部A股

3. **完整的回测系统**：
   - 基于PyBroker的回测框架
   - 支持多种交易策略
   - 详细的回测统计指标

## 安装依赖

```bash
pip install pandas numpy akshare talib pybroker
```

## 使用方法

### 1. 仅运行选股程序

```python
from stock_selector import StockSelector

# 创建选股器实例
selector = StockSelector(
    start_date='2022-01-01',  # 开始日期
    end_date='2022-12-31',    # 结束日期
    stock_pool='hs300',       # 股票池：'hs300', 'zz500', 'all_a'
    top_n=20,                 # 选择前20只股票
    use_fundamental=True      # 是否使用基本面数据
)

# 执行选股并保存结果
selected_stocks = selector.save_selected_stocks('selected_stocks.txt')

# 打印选出的股票
print("选出的股票:")
for stock in selected_stocks:
    print(stock)
```

### 2. 运行选股并回测

```bash
python run_backtest.py start_date=2022-01-01 end_date=2022-12-31 backtest_start=1/1/2023 backtest_end=6/30/2023 stock_pool=hs300 top_n=20 use_fundamental=true
```

或者在Python中调用：

```python
from run_backtest import run_selection_and_backtest

# 运行选股和回测
result = run_selection_and_backtest(
    start_date='2022-01-01',     # 选股开始日期
    end_date='2022-12-31',       # 选股结束日期
    backtest_start='1/1/2023',   # 回测开始日期
    backtest_end='6/30/2023',    # 回测结束日期
    stock_pool='hs300',          # 股票池
    top_n=20,                    # 选择前20只股票
    use_fundamental=True         # 是否使用基本面数据
)
```

## 参数说明

### 选股参数

- `start_date`：选股开始日期，格式 'YYYY-MM-DD'
- `end_date`：选股结束日期，格式 'YYYY-MM-DD'
- `stock_pool`：股票池，可选 'hs300'(沪深300), 'zz500'(中证500), 'all_a'(全部A股)
- `top_n`：选择排名前多少的股票
- `cache_dir`：数据缓存目录，默认为 './data_cache'
- `use_fundamental`：是否使用基本面数据进行选股，默认为 True

### 回测参数

- `backtest_start`：回测开始日期，格式 'M/D/YYYY'
- `backtest_end`：回测结束日期，格式 'M/D/YYYY'

## 回测策略说明

当前实现的回测策略是基于ROC（变化率）指标的轮动策略：

1. 对选出的股票计算20日ROC指标
2. 每个交易日，选择ROC值最高的前5只股票进行持有
3. 如果持有的股票不再属于ROC最高的前5只，则卖出
4. 如果未持有的股票进入ROC最高的前5只，则买入

## 自定义因子权重

您可以在 `stock_selector.py` 文件中修改 `factor_weights` 字典来调整各因子的权重：

```python
factor_weights = {
    # 动量因子 - 正向关系
    'momentum_20d': 0.1,
    'momentum_60d': 0.1,
    
    # 波动率因子 - 负向关系
    'volatility_20d': -0.05,
    'volatility_60d': -0.05,
    
    # 技术指标因子
    'rsi_14': 0.05,  # RSI高表示强势
    'macd_hist': 0.1,  # MACD柱状图正值表示上涨趋势
    'bb_position': 0.05,  # 价格在布林带下方有上涨空间
    
    # 成交量因子
    'volume_change_20d': 0.05,  # 成交量增加表示活跃度提高
    
    # 趋势因子
    'price_to_ma20': 0.1,  # 价格高于均线表示上涨趋势
    'price_to_ma60': 0.1,  # 价格高于均线表示上涨趋势
    
    # 反转因子
    'reversion_5d': 0.05,  # 从低点反弹的幅度
    
    # 基本面因子 - 如果可用
    'pe': -0.05,  # 市盈率低更好
    'pb': -0.05,  # 市净率低更好
    'market_cap': 0.03,  # 市值适中偏大
    'total_assets': 0.02,  # 总资产规模
    'net_profit': 0.05,  # 净利润高更好
    
    # ESG因子 - 如果可用
    'esg_score': 0.03,  # ESG评分高更好
    'env_score': 0.01,  # 环境评分
    'social_score': 0.01,  # 社会评分
    'gov_score': 0.01,  # 公司治理评分
}
```

## 基本面数据计算说明

系统通过以下方式计算基本面指标：

1. **市盈率(PE)**：总市值 / 净利润
2. **市净率(PB)**：总市值 / 净资产
3. **市值**：股价 * 总股本

这些数据通过 AKShare 获取财务报表和股价数据计算得出。如果您不想使用基本面数据（例如获取数据失败或速度太慢），可以设置 `use_fundamental=False`。

## 注意事项

1. 首次运行时，程序会下载并缓存股票数据，可能需要一些时间
2. 数据会缓存在 `./data_cache` 目录下，以加速后续运行
3. 如需更新数据，请删除缓存目录中的相关文件
4. 基本面数据获取可能会遇到API限制或网络问题，如果遇到问题可以设置 `use_fundamental=False` 仅使用技术指标进行选股 