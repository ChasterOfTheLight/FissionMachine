import akshare as ak

def convert_to_number(value):
    try:
        if '亿' in str(value):
            return float(str(value).replace('亿', '')) * 10000
        elif '万' in str(value):
            return float(str(value).replace('万', ''))
        return float(value)
    except:
        return 0

three_day_ranking = ak.stock_fund_flow_individual("3日排行")

# 资金流入净额转换为float数字
three_day_ranking["资金流入净额"] = three_day_ranking["资金流入净额"].apply(convert_to_number)

# 以资金流入净额为排序依据，降序排列
three_day_ranking = three_day_ranking.sort_values(by="资金流入净额", ascending=False)

# 股票代码不足6位的补0
three_day_ranking["股票代码"] = three_day_ranking["股票代码"].apply(lambda x: str(x).zfill(6))

# 股票代码转换为字符串
three_day_ranking["股票代码"] = three_day_ranking["股票代码"].apply(str)

# 输出排名前30的数据
print(three_day_ranking.head(30))

# 筛选股票代码为600050的数据
stock_data = three_day_ranking[three_day_ranking["股票代码"] == "600050"]
print(stock_data)