import akshare as ak
import pandas as pd
import numpy as np
import logging
import time
from datetime import datetime

logging.basicConfig(level=logging.INFO,
                   format="%(asctime)s - %(message)s", 
                   datefmt="%Y-%m-%d %H:%M:%S")

def get_hot_concepts():
    """获取热门概念板块"""
    try:
        concept_rank = ak.stock_board_concept_name_em()
        
        # 排除日涨停_含一字、昨日连板_含一字和昨日涨停
        concept_rank = concept_rank[~concept_rank['板块名称'].str.contains("日涨停_含一字")]
        concept_rank = concept_rank[~concept_rank['板块名称'].str.contains("昨日连板_含一字")]
        concept_rank = concept_rank[~concept_rank['板块名称'].str.contains("昨日连板")]

        # 添加综合评分列，使用实际可用的指标
        concept_rank['score'] = (
            concept_rank['涨跌幅'] * 0.4 +  # 涨跌幅权重40%
            concept_rank['换手率'].rank(pct=True) * 0.3 +  # 换手率权重30%
            (concept_rank['上涨家数'] / (concept_rank['上涨家数'] + concept_rank['下跌家数'])) * 0.3  # 板块强度权重30%
        )
        
        # 按照综合得分排序
        concept_rank = concept_rank.sort_values(by='score', ascending=False)
        
        # 打印热门概念详细信息
        logging.info("\n当前热门概念板块排名：\n" + str(concept_rank[['板块名称', '涨跌幅', '换手率', '上涨家数', '下跌家数', 'score']].head()))
        
        return concept_rank.head(5)['板块名称'].tolist()
    except Exception as e:
        logging.error(f"获取热门概念失败: {e}")
        return []

def get_concept_leading_stocks(concept_name):
    """获取概念板块龙头股"""
    try:
        # 获取概念成分股
        stocks = ak.stock_board_concept_cons_em(symbol=concept_name)
        # 按照市值和涨跌幅排序,找出龙头股
        stocks = stocks.sort_values(by=['成交额', '涨跌幅'], ascending=[False, False])
        # 返回代码和名称
        return stocks.head(5)[['代码', '名称']].to_dict('records')
    except Exception as e:
        logging.error(f"获取概念龙头股失败: {concept_name}: {e}")
        return []

def calculate_momentum_factors(stock_data):
    """计算动量相关因子"""
    # 计算BOLL通道
    stock_data['MA20'] = stock_data['收盘'].rolling(window=20).mean()
    stock_data['STD20'] = stock_data['收盘'].rolling(window=20).std()
    stock_data['BOLL_UP'] = stock_data['MA20'] + 2 * stock_data['STD20']
    stock_data['BOLL_DOWN'] = stock_data['MA20'] - 2 * stock_data['STD20']
    
    # 计算RSI
    delta = stock_data['收盘'].diff()
    gain = (delta.where(delta > 0, 0)).rolling(window=14).mean()
    loss = (-delta.where(delta < 0, 0)).rolling(window=14).mean()
    rs = gain / loss
    stock_data['RSI'] = 100 - (100 / (1 + rs))
    
    # 计算成交量放大倍数
    stock_data['Volume_Ratio'] = stock_data['成交量'] / stock_data['成交量'].rolling(window=5).mean()
    
    # 计算涨速
    stock_data['Price_Speed'] = stock_data['涨跌幅'].rolling(window=3).mean()
    
    # 计算主力资金净流入
    stock_data['Fund_Flow_Ratio'] = stock_data['成交额'].diff()
    
    return stock_data

def aggressive_stock_strategy(stock_data, stock_code):
    """激进选股策略"""
    if len(stock_data) < 20:
        return None
        
    # 计算技术指标
    stock_data = calculate_momentum_factors(stock_data)
    
    latest = stock_data.iloc[-1]
    
    # 创建打分系统
    score = 0
    
    # 因子1: 股价突破上轨(权重0.2)
    if latest['收盘'] > latest['BOLL_UP']:
        score += 0.2
        
    # 因子2: RSI强势但未超买(权重0.15)
    if 60 < latest['RSI'] < 80:
        score += 0.15
        
    # 因子3: 放量程度(权重0.2)
    if latest['Volume_Ratio'] > 2:  # 成交量是5日平均的2倍以上
        score += 0.2
        
    # 因子4: 涨速检测(权重0.15)
    if latest['Price_Speed'] > 3:  # 3日平均涨幅>3%
        score += 0.15
        
    # 因子5: 资金流入强度(权重0.15)
    if latest['Fund_Flow_Ratio'] > 0:
        score += 0.15
        
    # 因子6: 股价位置(权重0.15)
    if latest['收盘'] > latest['MA20']:
        score += 0.15

    return {
        '股票代码': stock_code,
        '日期': latest['日期'],
        '评分': score,
        'RSI': latest['RSI'],
        '成交量比': latest['Volume_Ratio'],
        '涨速': latest['涨跌幅']
    }

def run_strategy():
    """运行策略"""
    logging.info("开始执行热门概念龙头策略...")
    
    # 获取热门概念
    hot_concepts = get_hot_concepts()
    logging.info(f"当前热门概念: {hot_concepts}")
    
    recommendations = []
    
    for concept in hot_concepts:
        # 获取概念龙头股
        leading_stocks = get_concept_leading_stocks(concept)
        logging.info(f"{concept}概念龙头股: {leading_stocks}")
        
        for stock in leading_stocks:
            try:
                # 获取股票数据
                stock_data = ak.stock_zh_a_hist(symbol=stock['代码'],
                                              period="daily",
                                              end_date=datetime.now().strftime('%Y%m%d'),
                                              adjust="qfq")
                
                # 应用策略
                result = aggressive_stock_strategy(stock_data, stock['代码'])
                if result and result['评分'] >= 0.7:  # 只推荐评分大于0.7的股票
                    # 去重
                    if any(r['股票代码'] == result['股票代码'] for r in recommendations):
                        continue
                    result['所属概念'] = concept
                    result['股票名称'] = stock['名称']  # 添加股票名称
                    recommendations.append(result)
                    
            except Exception as e:
                logging.error(f"处理股票{stock['代码']}({stock['名称']})时出错: {e}")
                continue
                
            time.sleep(1)  # 避免请求过于频繁
    
    # 结果排序和展示
    if recommendations:
        df_results = pd.DataFrame(recommendations)
        # 调整列的顺序，把代码和名称放在前面
        cols = ['股票代码', '股票名称', '所属概念', '评分', 'RSI', '成交量比', '涨速', '日期']
        df_results = df_results[cols]
        df_results = df_results.sort_values('评分', ascending=False)
        logging.info("\n" + "="*50 + "\n概念龙头股推荐:\n" + str(df_results))
    else:
        logging.info("没有找到符合条件的股票")

run_strategy()