import akshare as ak
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import matplotlib.pyplot as plt
from typing import Dict, List, Tuple

class MarketSentimentAnalyzer:
    def __init__(self):
        self.today = datetime.now().strftime('%Y%m%d')
        self.yesterday = (datetime.now() - timedelta(days=1)).strftime('%Y%m%d')
        
    def get_market_overview(self) -> Dict:
        """获取市场整体概况"""
        try:
            # 获取A股市场指数
            index_df = ak.stock_zh_index_daily_em(symbol="sh000001")
            
            # 打印数据结构以便调试
            print("\n指数数据结构:")
            print(index_df.columns.tolist())
            print("\n数据示例:")
            print(index_df.head())
            
            if index_df.empty:
                print("获取到的指数数据为空")
                return {}
                
            latest = index_df.iloc[-1]
            previous = index_df.iloc[-2]  # 获取前一个交易日数据
            
            # 确保数据列存在
            required_columns = ['收盘', '成交量', '成交额']
            if not all(col in index_df.columns for col in required_columns):
                print(f"缺少必要的列: {required_columns}")
                print(f"可用的列: {index_df.columns.tolist()}")
                return {}
            
            # 计算涨跌幅
            pct_change = ((float(latest['收盘']) - float(previous['收盘'])) / float(previous['收盘'])) * 100
            
            # 获取市场情绪指标
            sentiment = ak.index_news_sentiment_scope()
            if sentiment.empty:
                print("获取到的情绪数据为空")
                return {}
                
            latest_sentiment = sentiment.iloc[-1]
            
            return {
                "上证指数": {
                    "当前值": latest["收盘"],
                    "今日涨跌幅": f"{pct_change:.2f}%",
                    "成交量": f"{float(latest['成交量'])/10000:.2f}万手",
                    "成交额": f"{float(latest['成交额'])/100000000:.2f}亿元"
                },
                "市场情绪": {
                    "新闻情绪指数": latest_sentiment["市场情绪指数"],
                    "市场情绪": "乐观" if float(latest_sentiment["市场情绪指数"]) > 1 else "悲观"
                }
            }
        except Exception as e:
            print(f"获取市场概况时出错: {str(e)}")
            print("错误详情:")
            import traceback
            print(traceback.format_exc())
            return {}

    def get_sector_flow(self) -> Tuple[pd.DataFrame, pd.DataFrame]:
        """获取行业资金流向"""
        try:
            # 获取行业资金流向
            sector_flow = ak.stock_sector_fund_flow_rank()
            
            # 计算资金流向
            sector_flow['资金流向'] = sector_flow['今日主力净流入-净额'] / 100000000  # 转换为亿元
            
            # 获取流入和流出板块
            inflow = sector_flow[sector_flow['资金流向'] > 0].sort_values('资金流向', ascending=False)
            outflow = sector_flow[sector_flow['资金流向'] < 0].sort_values('资金流向', ascending=True)
            
            return inflow, outflow
        except Exception as e:
            print(f"获取行业资金流向时出错: {str(e)}")
            return pd.DataFrame(), pd.DataFrame()

    def analyze_market_sentiment(self):
        """分析市场情绪并输出报告"""
        print("\n=== A股市场情绪分析报告 ===")
        print(f"报告日期: {datetime.now().strftime('%Y-%m-%d')}\n")
        
        # 获取市场概况
        overview = self.get_market_overview()
        if overview:
            print("【市场概况】")
            for category, data in overview.items():
                print(f"\n{category}:")
                for key, value in data.items():
                    print(f"  {key}: {value}")
        
        # 获取行业资金流向
        inflow, outflow = self.get_sector_flow()
        if not inflow.empty and not outflow.empty:
            print("\n【行业资金流向】")
            print("\n资金流入板块TOP10:")
            for _, row in inflow.head(10).iterrows():
                print(f"  {row['名称']}: {row['资金流向']:.2f}亿元")
            
            print("\n资金流出板块TOP10:")
            for _, row in outflow.head(10).iterrows():
                print(f"  {row['名称']}: {row['资金流向']:.2f}亿元")

def main():
    analyzer = MarketSentimentAnalyzer()
    analyzer.analyze_market_sentiment()

if __name__ == "__main__":
    main() 