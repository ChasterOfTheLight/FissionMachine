import pandas as pd
import numpy as np
import akshare as ak
import talib as ta
from datetime import datetime, timedelta
import os

class StockSelector:
    """
    多因子选股模型
    基于价值、成长、质量、动量、波动等多维度因子进行选股
    """
    
    def __init__(self, 
                 start_date=None, 
                 end_date=None,
                 stock_pool='hs300',
                 top_n=50,
                 cache_dir='./data_cache',
                 use_fundamental=True):
        """
        初始化选股器
        
        参数:
            start_date: 开始日期，格式 'YYYY-MM-DD'
            end_date: 结束日期，格式 'YYYY-MM-DD'
            stock_pool: 股票池，可选 'hs300'(沪深300), 'zz500'(中证500), 'all_a'(全部A股)
            top_n: 选择排名前多少的股票
            cache_dir: 数据缓存目录
            use_fundamental: 是否使用基本面数据进行选股，如果为False则仅使用技术指标
        """
        self.start_date = start_date or (datetime.now() - timedelta(days=365)).strftime('%Y-%m-%d')
        self.end_date = end_date or datetime.now().strftime('%Y-%m-%d')
        self.stock_pool = stock_pool
        self.top_n = top_n
        self.cache_dir = cache_dir
        self.use_fundamental = use_fundamental
        
        # 确保缓存目录存在
        if not os.path.exists(cache_dir):
            os.makedirs(cache_dir)
            
        # 初始化股票池
        self._init_stock_pool()
        
    def _init_stock_pool(self):
        """初始化股票池"""
        print(f"初始化股票池: {self.stock_pool}")
        
        if self.stock_pool == 'hs300':
            # 获取沪深300成分股
            self.stocks = self._get_hs300_stocks()
        elif self.stock_pool == 'zz500':
            # 获取中证500成分股
            self.stocks = self._get_zz500_stocks()
        elif self.stock_pool == 'all_a':
            # 获取所有A股
            self.stocks = self._get_all_a_stocks()
        else:
            raise ValueError(f"不支持的股票池: {self.stock_pool}")
            
        print(f"股票池初始化完成，共 {len(self.stocks)} 只股票")
    
    def _get_hs300_stocks(self):
        """获取沪深300成分股"""
        try:
            # 尝试从缓存加载
            cache_file = os.path.join(self.cache_dir, 'hs300_stocks.csv')
            if os.path.exists(cache_file):
                df = pd.read_csv(cache_file)
                return df['symbol'].tolist()
            
            # 从AKShare获取沪深300成分股
            df = ak.index_stock_cons_weight_csindex(symbol="000300")
            df['symbol'] = df['成分券代码'].apply(lambda x: x + '.SH' if x.startswith('6') else x + '.SZ')
            
            # 保存到缓存
            df[['symbol']].to_csv(cache_file, index=False)
            return df['symbol'].tolist()
        except Exception as e:
            print(f"获取沪深300成分股失败: {e}")
            # 如果失败，返回一些常见的沪深300成分股作为备选
            return ['600519.SH', '601318.SH', '600036.SH', '000858.SZ', '601166.SH', 
                    '000333.SZ', '600276.SH', '601888.SH', '600030.SH', '601012.SH']
    
    def _get_zz500_stocks(self):
        """获取中证500成分股"""
        try:
            # 尝试从缓存加载
            cache_file = os.path.join(self.cache_dir, 'zz500_stocks.csv')
            if os.path.exists(cache_file):
                df = pd.read_csv(cache_file)
                return df['symbol'].tolist()
            
            # 从AKShare获取中证500成分股
            df = ak.index_stock_cons_weight_csindex(symbol="000905")
            df['symbol'] = df['成分券代码'].apply(lambda x: x + '.SH' if x.startswith('6') else x + '.SZ')
            
            # 保存到缓存
            df[['symbol']].to_csv(cache_file, index=False)
            return df['symbol'].tolist()
        except Exception as e:
            print(f"获取中证500成分股失败: {e}")
            # 返回一些常见的中证500成分股作为备选
            return ['600009.SH', '600019.SH', '600021.SH', '600022.SH', '600026.SH',
                    '000009.SZ', '000012.SZ', '000021.SZ', '000025.SZ', '000027.SZ']
    
    def _get_all_a_stocks(self):
        """获取所有A股"""
        try:
            # 尝试从缓存加载
            cache_file = os.path.join(self.cache_dir, 'all_a_stocks.csv')
            if os.path.exists(cache_file):
                df = pd.read_csv(cache_file)
                return df['symbol'].tolist()
            
            # 从AKShare获取所有A股
            stock_zh_a_spot_df = ak.stock_zh_a_spot()
            
            # 转换为标准格式
            symbols = []
            for _, row in stock_zh_a_spot_df.iterrows():
                code = row['代码']
                if code.startswith('6'):
                    symbols.append(f"{code}.SH")
                else:
                    symbols.append(f"{code}.SZ")
            
            # 保存到缓存
            pd.DataFrame({'symbol': symbols}).to_csv(cache_file, index=False)
            return symbols
        except Exception as e:
            print(f"获取所有A股失败: {e}")
            # 返回一些常见股票作为备选
            return self._get_hs300_stocks()[:100]
    
    def _get_stock_data(self, symbol, start_date, end_date):
        """获取单只股票的历史数据"""
        try:
            # 尝试从缓存加载
            cache_file = os.path.join(self.cache_dir, f"{symbol.replace('.', '_')}.csv")
            if os.path.exists(cache_file):
                df = pd.read_csv(cache_file)
                df['date'] = pd.to_datetime(df['date'])
                # 过滤日期范围
                df = df[(df['date'] >= start_date) & (df['date'] <= end_date)]
                if not df.empty:
                    return df
            
            # 从AKShare获取股票数据
            stock_code = symbol.split('.')[0]
            market = 'sh' if symbol.endswith('.SH') else 'sz'
            
            # 获取日线数据
            df = ak.stock_zh_a_hist(symbol=stock_code, period="daily", 
                                    start_date=start_date.replace('-', ''), 
                                    end_date=end_date.replace('-', ''),
                                    adjust="qfq")
            
            # 重命名列
            df = df.rename(columns={
                '日期': 'date',
                '开盘': 'open',
                '收盘': 'close',
                '最高': 'high',
                '最低': 'low',
                '成交量': 'volume',
                '成交额': 'amount',
                '振幅': 'amplitude',
                '涨跌幅': 'pct_change',
                '涨跌额': 'change',
                '换手率': 'turnover'
            })
            
            # 转换日期格式
            df['date'] = pd.to_datetime(df['date'])
            
            # 保存到缓存
            df.to_csv(cache_file, index=False)
            return df
        except Exception as e:
            print(f"获取股票 {symbol} 数据失败: {e}")
            return pd.DataFrame()
    
    def _calculate_core_factors(self, df):
        """只计算核心因子"""
        if df.empty:
            return {}
        
        factors = {}
        
        # 1. 布林带位置 - 核心指标
        if len(df) >= 20:
            upper, middle, lower = ta.BBANDS(df['close'].values, timeperiod=20)
            # 价格在布林带中的位置(0-1)，越小表示越接近下轨
            factors['bb_position'] = (df['close'].iloc[-1] - lower[-1]) / (upper[-1] - lower[-1])
            # 是否在布林带下轨附近
            factors['is_bb_low'] = 1 if factors['bb_position'] < 0.2 else 0
            
        # 2. RSI - 用于确认超卖
        if len(df) >= 14:
            factors['rsi_14'] = ta.RSI(df['close'].values, timeperiod=14)[-1]
            factors['is_rsi_low'] = 1 if factors['rsi_14'] < 30 else 0
            
        return factors

    def _get_basic_info(self, symbol):
        """只获取必要的基本面信息"""
        try:
            stock_code = symbol.split('.')[0]
            
            # 获取市盈率等关键指标
            try:
                df = ak.stock_a_lg_indicator(symbol=stock_code)
                if not df.empty:
                    return {
                        'pe': df['pe'].iloc[0],  # 市盈率
                        'pb': df['pb'].iloc[0],  # 市净率
                        'market_cap': df['total_mv'].iloc[0]  # 总市值
                    }
            except:
                return {}
                
        except Exception as e:
            print(f"获取 {symbol} 基本面数据失败: {e}")
            return {}

    def select_stocks(self):
        """简化的选股流程"""
        print(f"开始选股...")
        
        qualified_stocks = []
        
        for symbol in self.stocks:
            try:
                # 1. 获取历史数据
                df = self._get_stock_data(symbol, self.start_date, self.end_date)
                if df.empty:
                    continue
                
                # 2. 计算技术指标
                factors = self._calculate_core_factors(df)
                if not factors:
                    continue
                
                # 3. 获取基本面数据
                basic_info = self._get_basic_info(symbol)
                factors.update(basic_info)
                
                # 4. 根据条件筛选
                if (factors.get('is_bb_low') == 1 and  # 布林带下轨附近
                    factors.get('is_rsi_low') == 1 and  # RSI低位
                    0 < factors.get('pe', float('inf')) < 30):  # 市盈率合理
                    
                    qualified_stocks.append({
                        'symbol': symbol,
                        'pe': factors.get('pe', '-'),
                        'rsi': factors.get('rsi_14', '-'),
                        'bb_pos': factors.get('bb_position', '-')
                    })
                    
            except Exception as e:
                print(f"处理 {symbol} 时出错: {e}")
                continue
        
        # 按市盈率排序
        qualified_stocks.sort(key=lambda x: x['pe'] if isinstance(x['pe'], (int, float)) else float('inf'))
        
        # 选择前N只股票
        selected = qualified_stocks[:self.top_n]
        
        # 获取股票名称并打印结果
        try:
            stock_names = {row['code']: row['name'] 
                         for _, row in ak.stock_info_a_code_name().iterrows()}
            
            print("\n选出的股票：")
            for i, stock in enumerate(selected, 1):
                code = stock['symbol'].split('.')[0]
                name = stock_names.get(code, '未知')
                print(f"{i}. {stock['symbol']} ({name})")
                print(f"   市盈率: {stock['pe']:.2f}, RSI: {stock['rsi']:.2f}, 布林带位置: {stock['bb_pos']:.2f}")
                
        except Exception as e:
            print(f"获取股票名称失败: {e}")
            
        return [stock['symbol'] for stock in selected]

    def save_selected_stocks(self, filename='selected_stocks.txt'):
        """保存选出的股票到文件"""
        selected_stocks = self.select_stocks()
        
        # 保存到文件
        with open(filename, 'w') as f:
            for stock in selected_stocks:
                f.write(f"{stock}\n")
        
        print(f"已将选出的股票保存到 {filename}")
        return selected_stocks


if __name__ == "__main__":
    # 使用示例
    selector = StockSelector(
        start_date='2025-01-01',
        end_date='2025-04-02',
        stock_pool='hs300',
        top_n=50,
        use_fundamental=True  # 设置是否使用基本面数据
    )
    
    # 执行选股并保存结果
    selected_stocks = selector.save_selected_stocks('selected_stocks.txt')
    
    # 打印选出的股票
    print("选出的股票:")
    for stock in selected_stocks:
        print(stock)