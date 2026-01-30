from smart_selector import SmartSelector

# 初始化选股器
selector = SmartSelector()

# 运行选股引擎，获取前10只推荐股票
top_stocks = selector.run(top_n=10, max_stocks=50)

# 查看选股结果
print(top_stocks)