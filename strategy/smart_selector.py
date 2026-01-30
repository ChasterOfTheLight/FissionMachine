import pandas as pd
import numpy as np
import logging
import time
import asyncio
import aiohttp
import threading
from datetime import datetime
from abc import ABC, abstractmethod
from functools import lru_cache
from concurrent.futures import ThreadPoolExecutor

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger("SmartSelector")


class DataSource(ABC):
    """数据源抽象基类"""
    
    @abstractmethod
    def get_all_stocks(self):
        """获取所有A股股票列表"""
        pass
    
    @abstractmethod
    def get_stock_data(self, stock_code):
        """获取个股详细数据"""
        pass
    
    @abstractmethod
    def get_fund_flow(self, stock_code):
        """获取资金流数据"""
        pass
    
    @abstractmethod
    def get_big_deal(self, stock_code):
        """获取大单交易数据"""
        pass
    
    @abstractmethod
    def get_chip_distribution(self, stock_code):
        """获取筹码分布数据"""
        pass
    
    @abstractmethod
    def get_popularity(self, stock_code):
        """获取热门度数据"""
        pass


class AkShareDataSource(DataSource):
    """AkShare数据源实现"""
    
    def __init__(self):
        try:
            import akshare as ak
            self.ak = ak
            self.available = True
            logger.info("AkShare数据源初始化成功")
        except ImportError:
            self.ak = None
            self.available = False
            logger.warning("AkShare库未安装，AkShare数据源不可用")
    
    def get_all_stocks(self):
        """获取所有A股股票列表"""
        if not self.available:
            logger.warning("AkShare数据源不可用，返回空数据")
            return pd.DataFrame()
        
        try:
            logger.info("使用AkShare获取所有A股股票列表...")
            stocks_df = self.ak.stock_zh_a_spot()
            logger.info(f"AkShare共获取到 {len(stocks_df)} 只股票")
            return stocks_df
        except Exception as e:
            logger.error(f"AkShare获取股票列表失败: {e}")
            return pd.DataFrame()
    
    def get_stock_data(self, stock_code):
        """获取个股详细数据"""
        if not self.available:
            return {}
        
        try:
            # 简化实现，实际项目中可根据需要获取更多数据
            return {"code": stock_code}
        except Exception as e:
            logger.error(f"AkShare获取个股数据失败 {stock_code}: {e}")
            return {}
    
    def get_fund_flow(self, stock_code):
        """获取资金流数据"""
        if not self.available:
            return {"net_inflow": 0, "net_inflow_rate": 0}
        
        try:
            logger.debug(f"使用AkShare获取 {stock_code} 资金流数据...")
            # 简化实现，实际项目中可调用具体的资金流接口
            return {
                "net_inflow": np.random.randint(0, 10000000),
                "net_inflow_rate": np.random.uniform(0, 0.2)
            }
        except Exception as e:
            logger.error(f"AkShare获取资金流数据失败 {stock_code}: {e}")
            return {"net_inflow": 0, "net_inflow_rate": 0}
    
    def get_big_deal(self, stock_code):
        """获取大单交易数据"""
        if not self.available:
            return {"buy_amount": 0, "sell_amount": 0, "buy_ratio": 0}
        
        try:
            logger.debug(f"使用AkShare获取 {stock_code} 大单数据...")
            # 简化实现，实际项目中可调用具体的大单交易接口
            buy_amount = np.random.randint(0, 5000000)
            sell_amount = np.random.randint(0, 3000000)
            total = buy_amount + sell_amount
            buy_ratio = buy_amount / total if total > 0 else 0
            return {
                "buy_amount": buy_amount,
                "sell_amount": sell_amount,
                "buy_ratio": buy_ratio
            }
        except Exception as e:
            logger.error(f"AkShare获取大单数据失败 {stock_code}: {e}")
            return {"buy_amount": 0, "sell_amount": 0, "buy_ratio": 0}
    
    def get_chip_distribution(self, stock_code):
        """获取筹码分布数据"""
        if not self.available:
            return {"profit_ratio": 0, "concentration_90": 0, "avg_cost": 0}
        
        try:
            logger.debug(f"使用AkShare获取 {stock_code} 筹码分布数据...")
            # 简化实现，实际项目中可调用具体的筹码分布接口
            return {
                "profit_ratio": np.random.uniform(0, 1),
                "concentration_90": np.random.uniform(0.1, 0.3),
                "avg_cost": np.random.uniform(5, 50)
            }
        except Exception as e:
            logger.error(f"AkShare获取筹码分布数据失败 {stock_code}: {e}")
            return {"profit_ratio": 0, "concentration_90": 0, "avg_cost": 0}
    
    def get_popularity(self, stock_code):
        """获取热门度数据"""
        if not self.available:
            return {"keyword_score": 0, "in_strong_pool": False, "is_zt": False}
        
        try:
            logger.debug(f"使用AkShare获取 {stock_code} 热门度数据...")
            # 简化实现，实际项目中可调用具体的热门度接口
            return {
                "keyword_score": np.random.randint(0, 200),
                "in_strong_pool": np.random.choice([True, False]),
                "is_zt": np.random.choice([True, False])
            }
        except Exception as e:
            logger.error(f"AkShare获取热门度数据失败 {stock_code}: {e}")
            return {"keyword_score": 0, "in_strong_pool": False, "is_zt": False}


class TuShareDataSource(DataSource):
    """TuShare数据源实现"""
    
    def __init__(self, token=None):
        try:
            import tushare as ts
            self.ts = ts
            if token:
                self.ts.set_token(token)
                self.pro = self.ts.pro_api()
            else:
                self.pro = None
            self.available = True
            logger.info("TuShare数据源初始化成功")
        except ImportError:
            self.ts = None
            self.pro = None
            self.available = False
            logger.warning("TuShare库未安装，TuShare数据源不可用")
    
    def get_all_stocks(self):
        """获取所有A股股票列表"""
        if not self.available or not self.pro:
            logger.warning("TuShare数据源不可用，返回空数据")
            return pd.DataFrame()
        
        try:
            logger.info("使用TuShare获取所有A股股票列表...")
            stocks_df = self.pro.stock_basic(exchange='', list_status='L', fields='ts_code,symbol,name,price,amount')
            # 转换格式以匹配AkShare
            if not stocks_df.empty:
                stocks_df.rename(columns={'ts_code': '代码', 'name': '名称', 'price': '最新价', 'amount': '成交量'}, inplace=True)
            logger.info(f"TuShare共获取到 {len(stocks_df)} 只股票")
            return stocks_df
        except Exception as e:
            logger.error(f"TuShare获取股票列表失败: {e}")
            return pd.DataFrame()
    
    def get_stock_data(self, stock_code):
        """获取个股详细数据"""
        if not self.available:
            return {}
        
        try:
            # 简化实现，实际项目中可根据需要获取更多数据
            return {"code": stock_code}
        except Exception as e:
            logger.error(f"TuShare获取个股数据失败 {stock_code}: {e}")
            return {}
    
    def get_fund_flow(self, stock_code):
        """获取资金流数据"""
        if not self.available:
            return {"net_inflow": 0, "net_inflow_rate": 0}
        
        try:
            logger.debug(f"使用TuShare获取 {stock_code} 资金流数据...")
            # 简化实现，实际项目中可调用具体的资金流接口
            return {
                "net_inflow": np.random.randint(0, 10000000),
                "net_inflow_rate": np.random.uniform(0, 0.2)
            }
        except Exception as e:
            logger.error(f"TuShare获取资金流数据失败 {stock_code}: {e}")
            return {"net_inflow": 0, "net_inflow_rate": 0}
    
    def get_big_deal(self, stock_code):
        """获取大单交易数据"""
        if not self.available:
            return {"buy_amount": 0, "sell_amount": 0, "buy_ratio": 0}
        
        try:
            logger.debug(f"使用TuShare获取 {stock_code} 大单数据...")
            # 简化实现，实际项目中可调用具体的大单交易接口
            buy_amount = np.random.randint(0, 5000000)
            sell_amount = np.random.randint(0, 3000000)
            total = buy_amount + sell_amount
            buy_ratio = buy_amount / total if total > 0 else 0
            return {
                "buy_amount": buy_amount,
                "sell_amount": sell_amount,
                "buy_ratio": buy_ratio
            }
        except Exception as e:
            logger.error(f"TuShare获取大单数据失败 {stock_code}: {e}")
            return {"buy_amount": 0, "sell_amount": 0, "buy_ratio": 0}
    
    def get_chip_distribution(self, stock_code):
        """获取筹码分布数据"""
        if not self.available:
            return {"profit_ratio": 0, "concentration_90": 0, "avg_cost": 0}
        
        try:
            logger.debug(f"使用TuShare获取 {stock_code} 筹码分布数据...")
            # 简化实现，实际项目中可调用具体的筹码分布接口
            return {
                "profit_ratio": np.random.uniform(0, 1),
                "concentration_90": np.random.uniform(0.1, 0.3),
                "avg_cost": np.random.uniform(5, 50)
            }
        except Exception as e:
            logger.error(f"TuShare获取筹码分布数据失败 {stock_code}: {e}")
            return {"profit_ratio": 0, "concentration_90": 0, "avg_cost": 0}
    
    def get_popularity(self, stock_code):
        """获取热门度数据"""
        if not self.available:
            return {"keyword_score": 0, "in_strong_pool": False, "is_zt": False}
        
        try:
            logger.debug(f"使用TuShare获取 {stock_code} 热门度数据...")
            # 简化实现，实际项目中可调用具体的热门度接口
            return {
                "keyword_score": np.random.randint(0, 200),
                "in_strong_pool": np.random.choice([True, False]),
                "is_zt": np.random.choice([True, False])
            }
        except Exception as e:
            logger.error(f"TuShare获取热门度数据失败 {stock_code}: {e}")
            return {"keyword_score": 0, "in_strong_pool": False, "is_zt": False}


class FallbackDataSource(DataSource):
    """备用数据源，当其他数据源都失败时使用"""
    
    def get_all_stocks(self):
        """获取所有A股股票列表"""
        logger.info("使用备用数据源获取股票列表...")
        # 创建测试股票列表
        test_stocks = pd.DataFrame({
            '代码': ['000001', '000002', '000004', '000005', '000006', '000007', '000008', '000009', '000010', '000011'],
            '名称': ['平安银行', '万科A', '白云机场', '世纪星源', '深振业A', '深达声A', '全新好', '中国宝安', '深深宝A', '深物业A'],
            '最新价': [15.0, 20.0, 18.0, 5.0, 8.0, 12.0, 15.0, 25.0, 10.0, 16.0],
            '成交量': [1000000, 2000000, 500000, 1500000, 800000, 1200000, 900000, 2500000, 700000, 1800000]
        })
        logger.info(f"备用数据源共获取到 {len(test_stocks)} 只股票")
        return test_stocks
    
    def get_stock_data(self, stock_code):
        """获取个股详细数据"""
        return {"code": stock_code}
    
    def get_fund_flow(self, stock_code):
        """获取资金流数据"""
        return {
            "net_inflow": np.random.randint(0, 10000000),
            "net_inflow_rate": np.random.uniform(0, 0.2)
        }
    
    def get_big_deal(self, stock_code):
        """获取大单交易数据"""
        buy_amount = np.random.randint(0, 5000000)
        sell_amount = np.random.randint(0, 3000000)
        total = buy_amount + sell_amount
        buy_ratio = buy_amount / total if total > 0 else 0
        return {
            "buy_amount": buy_amount,
            "sell_amount": sell_amount,
            "buy_ratio": buy_ratio
        }
    
    def get_chip_distribution(self, stock_code):
        """获取筹码分布数据"""
        return {
            "profit_ratio": np.random.uniform(0, 1),
            "concentration_90": np.random.uniform(0.1, 0.3),
            "avg_cost": np.random.uniform(5, 50)
        }
    
    def get_popularity(self, stock_code):
        """获取热门度数据"""
        return {
            "keyword_score": np.random.randint(0, 200),
            "in_strong_pool": np.random.choice([True, False]),
            "is_zt": np.random.choice([True, False])
        }


class DataSourceManager:
    """数据源管理器，负责管理多个数据源并在失败时自动切换"""
    
    def __init__(self, tushare_token=None):
        self.data_sources = [
            AkShareDataSource(),
            TuShareDataSource(tushare_token),
            FallbackDataSource()
        ]
        self.current_source_index = 0
        self.lock = threading.RLock()
    
    def get_current_source(self):
        """获取当前数据源"""
        with self.lock:
            return self.data_sources[self.current_source_index]
    
    def switch_to_next_source(self):
        """切换到下一个数据源"""
        with self.lock:
            self.current_source_index = (self.current_source_index + 1) % len(self.data_sources)
            current_source = self.data_sources[self.current_source_index]
            logger.info(f"切换到数据源: {current_source.__class__.__name__}")
            return current_source
    
    def get_all_stocks(self):
        """获取所有A股股票列表，自动切换数据源"""
        for i in range(len(self.data_sources)):
            source = self.get_current_source()
            stocks_df = source.get_all_stocks()
            if not stocks_df.empty:
                return stocks_df
            logger.warning(f"数据源 {source.__class__.__name__} 获取股票列表失败，尝试切换到下一个数据源")
            self.switch_to_next_source()
        
        logger.error("所有数据源都获取股票列表失败")
        return pd.DataFrame()
    
    def get_stock_data(self, stock_code):
        """获取个股详细数据"""
        source = self.get_current_source()
        return source.get_stock_data(stock_code)
    
    def get_fund_flow(self, stock_code):
        """获取资金流数据"""
        source = self.get_current_source()
        return source.get_fund_flow(stock_code)
    
    def get_big_deal(self, stock_code):
        """获取大单交易数据"""
        source = self.get_current_source()
        return source.get_big_deal(stock_code)
    
    def get_chip_distribution(self, stock_code):
        """获取筹码分布数据"""
        source = self.get_current_source()
        return source.get_chip_distribution(stock_code)
    
    def get_popularity(self, stock_code):
        """获取热门度数据"""
        source = self.get_current_source()
        return source.get_popularity(stock_code)


class SmartSelector:
    """智能选股器"""
    
    def __init__(self, tushare_token=None, max_workers=10, cache_expiry=3600):
        self.data_source_manager = DataSourceManager(tushare_token)
        self.weights = {
            'fund_flow': 0.3,
            'big_deal': 0.25,
            'chip_distribution': 0.2,
            'popularity': 0.25
        }
        self.executor = ThreadPoolExecutor(max_workers=max_workers)
        self.cache = {}
        self.cache_expiry = cache_expiry
        self.lock = threading.RLock()
        logger.info("智能选股器初始化成功")
    
    def _get_cached_data(self, key):
        """获取缓存数据"""
        with self.lock:
            if key in self.cache:
                data, timestamp = self.cache[key]
                if time.time() - timestamp < self.cache_expiry:
                    return data
                else:
                    # 缓存过期，删除
                    del self.cache[key]
            return None
    
    def _set_cached_data(self, key, data):
        """设置缓存数据"""
        with self.lock:
            self.cache[key] = (data, time.time())
    
    def filter_stocks(self, stocks_df):
        """过滤掉ST股票、停牌股票、北交所股票和科创板股票"""
        if stocks_df.empty:
            return stocks_df
        
        logger.info("正在过滤股票...")
        
        # 排除科创板股票（688开头）
        if '代码' in stocks_df.columns:
            filtered = stocks_df[~stocks_df['代码'].astype(str).str.startswith('688')]
        else:
            filtered = stocks_df
        
        # 排除ST股票
        if '名称' in filtered.columns:
            filtered = filtered[~filtered['名称'].astype(str).str.contains('ST')]
        
        # 排除停牌股票（成交量为0或最新价为0）
        if '成交量' in filtered.columns:
            filtered = filtered[filtered['成交量'] > 0]
        if '最新价' in filtered.columns:
            filtered = filtered[filtered['最新价'] > 0]

        # 排除北交所股票（以“83”、“87”、“88”、“bj”开头）
        if '代码' in filtered.columns:
            filtered = filtered[~filtered['代码'].astype(str).str.startswith(('83', '87', '88', 'bj'))]
        
        logger.info(f"过滤后剩余 {len(filtered)} 只股票")
        return filtered[['代码', '名称']] if not filtered.empty and all(col in filtered.columns for col in ['代码', '名称']) else filtered
    
    def calculate_single_stock_score(self, stock):
        """计算单个股票的综合评分"""
        try:
            stock_code = stock['代码']
            stock_name = stock['名称']
            
            logger.debug(f"正在处理 {stock_code} {stock_name}...")
            
            # 尝试从缓存获取数据
            cache_key = f"stock_score_{stock_code}"
            cached_score = self._get_cached_data(cache_key)
            if cached_score:
                logger.debug(f"从缓存获取 {stock_code} 的评分数据")
                return cached_score
            
            # 获取各维度数据
            fund_flow = self.data_source_manager.get_fund_flow(stock_code)
            big_deal = self.data_source_manager.get_big_deal(stock_code)
            chip_dist = self.data_source_manager.get_chip_distribution(stock_code)
            popularity = self.data_source_manager.get_popularity(stock_code)
            
            # 计算各维度评分（0-10分）
            # 资金流评分
            fund_score = min(10, max(0, (fund_flow.get('net_inflow_rate', 0) + 0.05) / 0.1 * 10))
            
            # 大单评分
            big_deal_score = min(10, max(0, big_deal.get('buy_ratio', 0) * 10))
            
            # 筹码分布评分
            # 获利比例越高，筹码越集中，评分越高
            chip_score = min(10, max(0, chip_dist.get('profit_ratio', 0) * 10 + (1 - chip_dist.get('concentration_90', 1)) * 5))
            
            # 热门度评分
            popular_score = popularity.get('keyword_score', 0) / 20 if popularity.get('keyword_score', 0) < 200 else 10
            if popularity.get('in_strong_pool', False):
                popular_score += 2
            if popularity.get('is_zt', False):
                popular_score += 3
            popular_score = min(10, popular_score)
            
            # 综合评分
            total_score = (
                fund_score * self.weights['fund_flow'] +
                big_deal_score * self.weights['big_deal'] +
                chip_score * self.weights['chip_distribution'] +
                popular_score * self.weights['popularity']
            )
            
            # 构建结果
            result = {
                '股票代码': stock_code,
                '股票名称': stock_name,
                '资金流评分': round(fund_score, 2),
                '大单评分': round(big_deal_score, 2),
                '筹码分布评分': round(chip_score, 2),
                '热门度评分': round(popular_score, 2),
                '综合评分': round(total_score, 2),
                '资金净流入': fund_flow.get('net_inflow', 0),
                '资金净流入率': fund_flow.get('net_inflow_rate', 0),
                '大单买入占比': big_deal.get('buy_ratio', 0),
                '获利比例': chip_dist.get('profit_ratio', 0),
                '筹码集中度': chip_dist.get('concentration_90', 0),
                '热门关键词得分': popularity.get('keyword_score', 0),
                '是否强势股': popularity.get('in_strong_pool', False),
                '是否涨停': popularity.get('is_zt', False)
            }
            
            # 缓存结果
            self._set_cached_data(cache_key, result)
            
            # 避免请求过于频繁
            time.sleep(0.1)
            
            return result
        except Exception as e:
            logger.error(f"计算股票评分失败 {stock.get('代码', '未知')}: {e}")
            return {
                '股票代码': stock.get('代码', '未知'),
                '股票名称': stock.get('名称', '未知'),
                '资金流评分': 0,
                '大单评分': 0,
                '筹码分布评分': 0,
                '热门度评分': 0,
                '综合评分': 0
            }
    
    def calculate_scores(self, stock_list):
        """计算股票的综合评分（并行处理）"""
        if stock_list.empty:
            return pd.DataFrame()
        
        logger.info(f"正在计算 {len(stock_list)} 只股票的评分...")
        
        # 并行计算评分
        results = []
        stocks = [stock for _, stock in stock_list.iterrows()]
        
        # 使用线程池并行处理
        futures = []
        for stock in stocks:
            future = self.executor.submit(self.calculate_single_stock_score, stock)
            futures.append(future)
        
        # 收集结果
        for future in futures:
            try:
                result = future.result()
                results.append(result)
            except Exception as e:
                logger.error(f"处理股票时出错: {e}")
        
        logger.info(f"完成 {len(results)} 只股票的评分计算")
        return pd.DataFrame(results)
    
    def select_top_stocks(self, results, top_n=10):
        """选择综合评分最高的股票"""
        if results.empty:
            return results
        
        logger.info(f"正在选择前 {top_n} 只股票...")
        # 按综合评分排序
        sorted_results = results.sort_values(by='综合评分', ascending=False)
        return sorted_results.head(top_n)
    
    def run(self, top_n=10, max_stocks=100):
        """运行选股引擎"""
        logger.info("开始运行智能选股引擎...")
        
        # 获取所有股票
        all_stocks = self.data_source_manager.get_all_stocks()
        if all_stocks.empty:
            logger.error("未获取到股票列表")
            return pd.DataFrame()
        
        # 过滤股票
        filtered_stocks = self.filter_stocks(all_stocks)
        if filtered_stocks.empty:
            logger.error("过滤后没有符合条件的股票")
            return pd.DataFrame()
        
        # 限制处理股票数量以提高性能
        if len(filtered_stocks) > max_stocks:
            filtered_stocks = filtered_stocks.head(max_stocks)
            logger.info(f"限制处理股票数量为 {max_stocks} 只")
        
        # 计算评分
        results = self.calculate_scores(filtered_stocks)
        if results.empty:
            logger.error("未计算出股票评分")
            return pd.DataFrame()
        
        # 选择前N只股票
        top_stocks = self.select_top_stocks(results, top_n)
        
        logger.info(f"选股完成，共推荐 {len(top_stocks)} 只股票")
        return top_stocks
    
    def __del__(self):
        """析构函数，关闭线程池"""
        if hasattr(self, 'executor'):
            self.executor.shutdown(wait=False)


class AsyncSmartSelector:
    """异步智能选股器，使用asyncio提高性能"""
    
    def __init__(self, tushare_token=None, cache_expiry=3600):
        self.data_source_manager = DataSourceManager(tushare_token)
        self.weights = {
            'fund_flow': 0.3,
            'big_deal': 0.25,
            'chip_distribution': 0.2,
            'popularity': 0.25
        }
        self.cache = {}
        self.cache_expiry = cache_expiry
        self.lock = asyncio.Lock()
        logger.info("异步智能选股器初始化成功")
    
    async def _get_cached_data(self, key):
        """获取缓存数据"""
        async with self.lock:
            if key in self.cache:
                data, timestamp = self.cache[key]
                if time.time() - timestamp < self.cache_expiry:
                    return data
                else:
                    # 缓存过期，删除
                    del self.cache[key]
            return None
    
    async def _set_cached_data(self, key, data):
        """设置缓存数据"""
        async with self.lock:
            self.cache[key] = (data, time.time())
    
    def filter_stocks(self, stocks_df):
        """过滤掉ST股票、停牌股票、北交所股票和科创板股票"""
        if stocks_df.empty:
            return stocks_df
        
        logger.info("正在过滤股票...")
        
        # 排除科创板股票（688开头）
        if '代码' in stocks_df.columns:
            filtered = stocks_df[~stocks_df['代码'].astype(str).str.startswith('688')]
        else:
            filtered = stocks_df
        
        # 排除ST股票
        if '名称' in filtered.columns:
            filtered = filtered[~filtered['名称'].astype(str).str.contains('ST')]
        
        # 排除停牌股票（成交量为0或最新价为0）
        if '成交量' in filtered.columns:
            filtered = filtered[filtered['成交量'] > 0]
        if '最新价' in filtered.columns:
            filtered = filtered[filtered['最新价'] > 0]

        # 排除北交所股票（以“83”、“87”、“88”、“bj”开头）
        if '代码' in filtered.columns:
            filtered = filtered[~filtered['代码'].astype(str).str.startswith(('83', '87', '88', 'bj'))]
        
        logger.info(f"过滤后剩余 {len(filtered)} 只股票")
        return filtered[['代码', '名称']] if not filtered.empty and all(col in filtered.columns for col in ['代码', '名称']) else filtered
    
    async def calculate_single_stock_score(self, stock):
        """计算单个股票的综合评分"""
        try:
            stock_code = stock['代码']
            stock_name = stock['名称']
            
            logger.debug(f"正在处理 {stock_code} {stock_name}...")
            
            # 尝试从缓存获取数据
            cache_key = f"stock_score_{stock_code}"
            cached_score = await self._get_cached_data(cache_key)
            if cached_score:
                logger.debug(f"从缓存获取 {stock_code} 的评分数据")
                return cached_score
            
            # 获取各维度数据（异步）
            # 注意：这里仍然是同步调用，实际项目中可根据数据源实现异步接口
            fund_flow = self.data_source_manager.get_fund_flow(stock_code)
            big_deal = self.data_source_manager.get_big_deal(stock_code)
            chip_dist = self.data_source_manager.get_chip_distribution(stock_code)
            popularity = self.data_source_manager.get_popularity(stock_code)
            
            # 计算各维度评分（0-10分）
            # 资金流评分
            fund_score = min(10, max(0, (fund_flow.get('net_inflow_rate', 0) + 0.05) / 0.1 * 10))
            
            # 大单评分
            big_deal_score = min(10, max(0, big_deal.get('buy_ratio', 0) * 10))
            
            # 筹码分布评分
            chip_score = min(10, max(0, chip_dist.get('profit_ratio', 0) * 10 + (1 - chip_dist.get('concentration_90', 1)) * 5))
            
            # 热门度评分
            popular_score = popularity.get('keyword_score', 0) / 20 if popularity.get('keyword_score', 0) < 200 else 10
            if popularity.get('in_strong_pool', False):
                popular_score += 2
            if popularity.get('is_zt', False):
                popular_score += 3
            popular_score = min(10, popular_score)
            
            # 综合评分
            total_score = (
                fund_score * self.weights['fund_flow'] +
                big_deal_score * self.weights['big_deal'] +
                chip_score * self.weights['chip_distribution'] +
                popular_score * self.weights['popularity']
            )
            
            # 构建结果
            result = {
                '股票代码': stock_code,
                '股票名称': stock_name,
                '资金流评分': round(fund_score, 2),
                '大单评分': round(big_deal_score, 2),
                '筹码分布评分': round(chip_score, 2),
                '热门度评分': round(popular_score, 2),
                '综合评分': round(total_score, 2),
                '资金净流入': fund_flow.get('net_inflow', 0),
                '资金净流入率': fund_flow.get('net_inflow_rate', 0),
                '大单买入占比': big_deal.get('buy_ratio', 0),
                '获利比例': chip_dist.get('profit_ratio', 0),
                '筹码集中度': chip_dist.get('concentration_90', 0),
                '热门关键词得分': popularity.get('keyword_score', 0),
                '是否强势股': popularity.get('in_strong_pool', False),
                '是否涨停': popularity.get('is_zt', False)
            }
            
            # 缓存结果
            await self._set_cached_data(cache_key, result)
            
            # 避免请求过于频繁
            await asyncio.sleep(0.1)
            
            return result
        except Exception as e:
            logger.error(f"计算股票评分失败 {stock.get('代码', '未知')}: {e}")
            return {
                '股票代码': stock.get('代码', '未知'),
                '股票名称': stock.get('名称', '未知'),
                '资金流评分': 0,
                '大单评分': 0,
                '筹码分布评分': 0,
                '热门度评分': 0,
                '综合评分': 0
            }
    
    async def calculate_scores(self, stock_list):
        """计算股票的综合评分（异步处理）"""
        if stock_list.empty:
            return pd.DataFrame()
        
        logger.info(f"正在异步计算 {len(stock_list)} 只股票的评分...")
        
        # 异步计算评分
        tasks = []
        for _, stock in stock_list.iterrows():
            task = self.calculate_single_stock_score(stock)
            tasks.append(task)
        
        # 收集结果
        results = []
        for task in asyncio.as_completed(tasks):
            try:
                result = await task
                results.append(result)
            except Exception as e:
                logger.error(f"处理股票时出错: {e}")
        
        logger.info(f"完成 {len(results)} 只股票的评分计算")
        return pd.DataFrame(results)
    
    async def run_async(self, top_n=10, max_stocks=100):
        """异步运行选股引擎"""
        logger.info("开始异步运行智能选股引擎...")
        
        # 获取所有股票
        all_stocks = self.data_source_manager.get_all_stocks()
        if all_stocks.empty:
            logger.error("未获取到股票列表")
            return pd.DataFrame()
        
        # 过滤股票
        filtered_stocks = self.filter_stocks(all_stocks)
        if filtered_stocks.empty:
            logger.error("过滤后没有符合条件的股票")
            return pd.DataFrame()
        
        # 限制处理股票数量以提高性能
        if len(filtered_stocks) > max_stocks:
            filtered_stocks = filtered_stocks.head(max_stocks)
            logger.info(f"限制处理股票数量为 {max_stocks} 只")
        
        # 计算评分
        results = await self.calculate_scores(filtered_stocks)
        if results.empty:
            logger.error("未计算出股票评分")
            return pd.DataFrame()
        
        # 选择前N只股票
        if not results.empty:
            logger.info(f"正在选择前 {top_n} 只股票...")
            # 按综合评分排序
            sorted_results = results.sort_values(by='综合评分', ascending=False)
            top_stocks = sorted_results.head(top_n)
            logger.info(f"选股完成，共推荐 {len(top_stocks)} 只股票")
            return top_stocks
        
        return pd.DataFrame()
    
    def run(self, top_n=10, max_stocks=100):
        """同步运行选股引擎（内部调用异步版本）"""
        return asyncio.run(self.run_async(top_n, max_stocks))


def main():
    """主函数"""
    # 初始化智能选股器
    selector = SmartSelector()
    
    # 运行选股引擎
    top_stocks = selector.run(top_n=10, max_stocks=50)
    
    if not top_stocks.empty:
        logger.info("\n" + "="*80)
        logger.info("智能选股结果")
        logger.info("="*80)
        # 打印结果
        print(top_stocks[['股票代码', '股票名称', '资金流评分', '大单评分', '筹码分布评分', '热门度评分', '综合评分']])
        
        # 保存结果到文件
        output_file = f"智能选股结果_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
        top_stocks.to_csv(output_file, index=False, encoding='utf-8-sig')
        logger.info(f"选股结果已保存到 {output_file}")
    else:
        logger.info("未找到符合条件的股票")


def main_async():
    """异步主函数"""
    # 初始化异步智能选股器
    selector = AsyncSmartSelector()
    
    # 运行选股引擎
    top_stocks = selector.run(top_n=10, max_stocks=50)
    
    if not top_stocks.empty:
        logger.info("\n" + "="*80)
        logger.info("异步智能选股结果")
        logger.info("="*80)
        # 打印结果
        print(top_stocks[['股票代码', '股票名称', '资金流评分', '大单评分', '筹码分布评分', '热门度评分', '综合评分']])
        
        # 保存结果到文件
        output_file = f"异步智能选股结果_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
        top_stocks.to_csv(output_file, index=False, encoding='utf-8-sig')
        logger.info(f"选股结果已保存到 {output_file}")
    else:
        logger.info("未找到符合条件的股票")


if __name__ == "__main__":
    # 运行同步版本
    main()
    
    # 运行异步版本
    # main_async()
