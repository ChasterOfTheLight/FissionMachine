import sys
import os
import pytest
import pandas as pd

# 添加项目根目录到路径
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from complex.stock_selector import StockSelector

def test_stock_selector_init():
    """测试选股引擎初始化"""
    selector = StockSelector()
    assert selector is not None
    assert selector.weights['fund_flow'] == 0.3
    assert selector.weights['big_deal'] == 0.25
    assert selector.weights['chip_distribution'] == 0.2
    assert selector.weights['popularity'] == 0.25

def test_get_all_stocks():
    """测试获取股票列表"""
    selector = StockSelector()
    stocks = selector.get_all_stocks()
    # 检查是否返回DataFrame
    assert isinstance(stocks, pd.DataFrame)
    # 检查是否包含必要的列
    assert '代码' in stocks.columns
    assert '名称' in stocks.columns
    assert '最新价' in stocks.columns
    assert '成交量' in stocks.columns

def test_filter_stocks():
    """测试股票筛选功能"""
    selector = StockSelector()
    # 创建测试数据
    test_data = pd.DataFrame({
        '代码': ['688001', '000001', '000002', '000003', '000004', 'ST股票'],
        '名称': ['科创板股票', '平安银行', '万科A', 'ST股票', '招商地产', 'ST测试'],
        '成交量': [1000, 2000, 3000, 0, 5000, 6000],
        '最新价': [10, 20, 30, 0, 50, 60]
    })
    
    filtered = selector.filter_stocks(test_data)
    
    # 检查过滤结果
    assert len(filtered) == 2  # 应该只保留000001和000002
    assert '688001' not in filtered['代码'].values  # 排除科创板
    assert 'ST测试' not in filtered['名称'].values  # 排除ST股票
    assert '000003' not in filtered['代码'].values  # 排除停牌股票（成交量为0）
    assert '000004' not in filtered['代码'].values  # 排除名称包含ST的股票

def test_get_fund_flow():
    """测试资金流数据获取"""
    selector = StockSelector()
    # 测试获取资金流数据
    fund_flow = selector.get_fund_flow('000001')
    assert isinstance(fund_flow, dict)
    assert 'net_inflow' in fund_flow
    assert 'net_inflow_rate' in fund_flow

def test_get_big_deal():
    """测试大单数据获取"""
    selector = StockSelector()
    # 测试获取大单数据
    big_deal = selector.get_big_deal('000001')
    assert isinstance(big_deal, dict)
    assert 'buy_amount' in big_deal
    assert 'sell_amount' in big_deal
    assert 'buy_ratio' in big_deal

def test_get_chip_distribution():
    """测试筹码分布数据获取"""
    selector = StockSelector()
    # 测试获取筹码分布数据
    chip_dist = selector.get_chip_distribution('000001')
    assert isinstance(chip_dist, dict)
    assert 'profit_ratio' in chip_dist
    assert 'concentration_90' in chip_dist
    assert 'avg_cost' in chip_dist

def test_get_popularity():
    """测试热门度数据获取"""
    selector = StockSelector()
    # 测试获取热门度数据
    popularity = selector.get_popularity('000001')
    assert isinstance(popularity, dict)
    assert 'keyword_score' in popularity
    assert 'in_strong_pool' in popularity
    assert 'is_zt' in popularity

def test_calculate_scores():
    """测试评分计算功能"""
    selector = StockSelector()
    # 创建测试数据
    test_stocks = pd.DataFrame({
        '代码': ['000001', '000002'],
        '名称': ['平安银行', '万科A']
    })
    
    # 测试评分计算
    results = selector.calculate_scores(test_stocks)
    assert isinstance(results, pd.DataFrame)
    assert len(results) == 2
    assert '股票代码' in results.columns
    assert '综合评分' in results.columns
    assert all(results['综合评分'] >= 0)
    assert all(results['综合评分'] <= 10)

def test_select_top_stocks():
    """测试选择前N只股票"""
    selector = StockSelector()
    # 创建测试数据
    test_results = pd.DataFrame({
        '股票代码': ['000001', '000002', '000003', '000004', '000005'],
        '股票名称': ['平安银行', '万科A', '招商地产', '保利地产', '金地集团'],
        '综合评分': [8.5, 9.2, 7.8, 8.9, 9.5]
    })
    
    # 测试选择前3只股票
    top_stocks = selector.select_top_stocks(test_results, 3)
    assert len(top_stocks) == 3
    # 检查是否按综合评分降序排序
    assert top_stocks['综合评分'].iloc[0] == 9.5
    assert top_stocks['综合评分'].iloc[1] == 9.2
    assert top_stocks['综合评分'].iloc[2] == 8.9

def test_integration():
    """测试完整的选股流程"""
    selector = StockSelector()
    # 测试完整的选股流程，只选择2只股票以提高测试速度
    top_stocks = selector.run(2)
    assert isinstance(top_stocks, pd.DataFrame)
    assert len(top_stocks) <= 2
    if len(top_stocks) > 0:
        assert '股票代码' in top_stocks.columns
        assert '股票名称' in top_stocks.columns
        assert '综合评分' in top_stocks.columns

if __name__ == '__main__':
    # 运行所有测试
    pytest.main(['-v', __file__])
