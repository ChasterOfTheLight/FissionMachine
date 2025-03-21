import akshare as ak
import pandas as pd
from datetime import datetime

def analyze_volume_trend():
    """分析最近10个交易日的开盘量能趋势"""
    try:
        # 获取上证指数日线数据
        df = ak.stock_zh_index_daily_em(symbol="sh000001")
        
        # 获取最近10个交易日数据
        recent_data = df.tail(10)
        
        # 计算成交量和成交额
        recent_data['成交量(万手)'] = recent_data['volume'].astype(float) / 10000
        recent_data['成交额(亿元)'] = recent_data['amount'].astype(float) / 100000000
        
        print(f"\n=== 最近{len(recent_data)}个交易日量能分析 ===")
        print(f"统计日期: {datetime.now().strftime('%Y-%m-%d')}\n")
        
        # 打印每日量能数据
        for _, row in recent_data.iterrows():
            print(f"日期: {row['date']}")
            print(f"  开盘价: {row['open']:.2f}")
            print(f"  收盘价: {row['close']:.2f}")
            print(f"  成交量: {row['成交量(万手)']:.2f}万手")
            print(f"  成交额: {row['成交额(亿元)']:.2f}亿元\n")
        
        # 计算量能趋势
        avg_volume = recent_data['成交量(万手)'].mean()
        latest_volume = recent_data['成交量(万手)'].iloc[-1]
        
        print("量能趋势分析:")
        if latest_volume > avg_volume * 1.1:
            print("当前成交量高于近期平均水平，市场活跃度上升")
        elif latest_volume < avg_volume * 0.9:
            print("当前成交量低于近期平均水平，市场活跃度下降")
        else:
            print("当前成交量处于近期平均水平，市场活跃度平稳")
            
    except Exception as e:
        print(f"分析量能趋势时出错: {str(e)}")

if __name__ == "__main__":
    analyze_volume_trend()