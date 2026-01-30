"""
简单实用的选股工具
功能：基于技术指标和基本面快速筛选优质股票
"""

import akshare as ak
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import warnings
import sys
import io

# 设置输出编码为UTF-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

warnings.filterwarnings('ignore')


class SimpleStockPicker:
    """简单选股器 - 专注实战"""
    
    def __init__(self):
        print("=" * 60)
        print("简单选股器 v1.0")
        print("=" * 60)
    
    def get_all_stocks(self):
        """获取所有A股列表"""
        print("\n[*] 正在获取A股列表...")
        
        # 尝试多个数据源
        data_sources = [
            ('东方财富', lambda: ak.stock_zh_a_spot_em()),
            ('新浪财经', lambda: ak.stock_zh_a_spot()),
        ]
        
        for source_name, source_func in data_sources:
            try:
                print(f"    尝试数据源: {source_name}...")
                df = source_func()
                print(f"[OK] 成功从{source_name}获取 {len(df)} 只股票")
                return df
            except Exception as e:
                print(f"    {source_name}失败: {str(e)[:80]}")
                continue
        
        # 所有数据源都失败，返回模拟数据用于测试
        print("[WARNING] 所有数据源失败，使用模拟数据进行演示")
        return self._get_mock_data()
    
    def _get_mock_data(self):
        """生成模拟数据用于测试"""
        import random
        
        mock_stocks = []
        stock_names = ['贵州茅台', '宁德时代', '比亚迪', '中国平安', '招商银行', 
                      '美的集团', '五粮液', '隆基绿能', '万科A', '格力电器',
                      '长江电力', '海天味业', '中国石油', '中国移动', '工商银行']
        
        for i, name in enumerate(stock_names):
            code = f"{'6' if i % 2 == 0 else '0'}0000{i+1:01d}"
            price = random.uniform(10, 200)
            mock_stocks.append({
                '代码': code,
                '名称': name,
                '最新价': round(price, 2),
                '涨跌幅': round(random.uniform(-5, 8), 2),
                '成交量': random.randint(50000, 500000),
                '换手率': round(random.uniform(1, 15), 2),
                '量比': round(random.uniform(0.8, 2.5), 2),
                '振幅': round(random.uniform(1, 10), 2),
                '市盈率-动态': round(random.uniform(5, 50), 2),
                '市净率': round(random.uniform(0.5, 5), 2),
                '流通市值': random.randint(100_0000_0000, 1000_0000_0000),
                '60日涨跌幅': round(random.uniform(-20, 50), 2)
            })
        
        return pd.DataFrame(mock_stocks)
    
    def filter_basic(self, df):
        """基础过滤：排除ST、停牌等"""
        print("\n[*] 开始基础过滤...")
        
        original_count = len(df)
        
        # 排除ST股票
        df = df[~df['名称'].str.contains('ST|退', na=False)]
        
        # 排除科创板(sh688)、北交所(bj/sh8/sh4开头)
        if '代码' in df.columns:
            # 规范化代码格式
            df['代码_简化'] = df['代码'].str.replace('sh', '').str.replace('sz', '').str.replace('bj', '')
            df = df[~df['代码_简化'].str.startswith(('688', '8', '4'))]
            df = df[~df['代码'].str.startswith('bj')]
            df = df.drop('代码_简化', axis=1)
        
        # 排除停牌（成交量为0）
        df = df[df['成交量'] > 0]
        
        # 排除价格异常
        df = df[(df['最新价'] > 1) & (df['最新价'] < 300)]
        
        filtered_count = len(df)
        print(f"   过滤前: {original_count} 只")
        print(f"   过滤后: {filtered_count} 只")
        print(f"   排除了: {original_count - filtered_count} 只")
        
        return df
    
    def calculate_technical_score(self, row):
        """计算技术面评分 (0-100分)"""
        score = 0
        
        # 1. 涨跌幅评分 (20分)
        change_pct = row.get('涨跌幅', 0)
        if 0 < change_pct < 3:
            score += 15
        elif 3 <= change_pct < 7:
            score += 20
        elif change_pct >= 7:
            score += 10
        
        # 2. 成交量评分 (20分)
        volume = row.get('成交量', 0)
        if volume > 100000:  # 10万手以上
            score += 20
        elif volume > 50000:
            score += 15
        elif volume > 10000:
            score += 10
        
        # 3. 换手率评分 (20分)
        turnover = row.get('换手率', 0)
        if 2 < turnover < 10:  # 适中换手率
            score += 20
        elif 10 <= turnover < 20:
            score += 15
        elif turnover >= 20:
            score += 5
        
        # 4. 振幅评分 (20分)
        amplitude = row.get('振幅', 0)
        if 2 < amplitude < 8:
            score += 20
        elif amplitude >= 8:
            score += 10
        
        # 5. 量比评分 (20分)
        volume_ratio = row.get('量比', 1)
        if volume_ratio > 1.5:
            score += 20
        elif volume_ratio > 1.2:
            score += 15
        elif volume_ratio > 1:
            score += 10
        
        return score
    
    def calculate_fundamental_score(self, row):
        """计算基本面评分 (0-100分)"""
        score = 0
        
        # 1. 市盈率评分 (30分)
        pe = row.get('市盈率-动态', 0)
        if 0 < pe < 20:
            score += 30
        elif 20 <= pe < 50:
            score += 20
        elif pe < 0:
            score += 0
        else:
            score += 10
        
        # 2. 市净率评分 (30分)
        pb = row.get('市净率', 0)
        if 0 < pb < 2:
            score += 30
        elif 2 <= pb < 5:
            score += 20
        else:
            score += 10
        
        # 3. 流通市值评分 (20分)
        market_cap = row.get('流通市值', 0)
        if 50_0000_0000 < market_cap < 500_0000_0000:  # 50-500亿
            score += 20
        elif market_cap >= 500_0000_0000:
            score += 15
        else:
            score += 10
        
        # 4. 60日涨跌幅评分 (20分)
        change_60d = row.get('60日涨跌幅', 0)
        if change_60d > 20:
            score += 20
        elif change_60d > 10:
            score += 15
        elif change_60d > 0:
            score += 10
        
        return score
    
    def rank_stocks(self, df, top_n=20):
        """股票评分排名"""
        print("\n[*] 正在计算股票评分...")
        
        # 计算技术面和基本面评分
        df['技术面评分'] = df.apply(self.calculate_technical_score, axis=1)
        df['基本面评分'] = df.apply(self.calculate_fundamental_score, axis=1)
        
        # 综合评分（技术面60%，基本面40%）
        df['综合评分'] = df['技术面评分'] * 0.6 + df['基本面评分'] * 0.4
        
        # 按综合评分排序
        df = df.sort_values('综合评分', ascending=False)
        
        # 选择Top N
        result = df.head(top_n)
        
        print(f"[OK] 评分完成，筛选出前 {top_n} 只股票")
        
        return result
    
    def format_output(self, df):
        """格式化输出"""
        output_columns = [
            '代码', '名称', '最新价', '涨跌幅', 
            '换手率', '量比', '振幅',
            '市盈率-动态', '市净率', '流通市值',
            '技术面评分', '基本面评分', '综合评分'
        ]
        
        # 只选择存在的列
        existing_columns = [col for col in output_columns if col in df.columns]
        result = df[existing_columns].copy()
        
        # 格式化数值
        if '涨跌幅' in result.columns:
            result['涨跌幅'] = result['涨跌幅'].apply(lambda x: f"{x:.2f}%")
        if '换手率' in result.columns:
            result['换手率'] = result['换手率'].apply(lambda x: f"{x:.2f}%")
        if '流通市值' in result.columns:
            result['流通市值'] = result['流通市值'].apply(lambda x: f"{x/100000000:.2f}亿")
        
        return result
    
    def save_result(self, df, filename=None):
        """保存结果到CSV"""
        if filename is None:
            filename = f"选股结果_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
        
        df.to_csv(filename, index=False, encoding='utf-8-sig')
        print(f"\n[SAVED] 结果已保存到: {filename}")
    
    def run(self, top_n=20, save=True):
        """运行选股"""
        # 1. 获取股票列表
        stocks = self.get_all_stocks()
        if stocks.empty:
            print("[ERROR] 无法获取股票数据")
            return None
        
        # 2. 基础过滤
        stocks = self.filter_basic(stocks)
        if stocks.empty:
            print("[ERROR] 过滤后没有符合条件的股票")
            return None
        
        # 3. 评分排名
        result = self.rank_stocks(stocks, top_n)
        
        # 4. 格式化输出
        output = self.format_output(result)
        
        # 5. 显示结果
        print("\n" + "=" * 60)
        print("选股结果 (按综合评分排序)")
        print("=" * 60)
        print(output.to_string(index=False))
        
        # 6. 保存结果
        if save:
            self.save_result(result)
        
        return result


def quick_pick(top_n=20):
    """快速选股 - 一行代码搞定"""
    picker = SimpleStockPicker()
    return picker.run(top_n=top_n)


def main():
    """主函数"""
    # 快速选股，显示前20只
    quick_pick(top_n=20)


if __name__ == "__main__":
    main()
