import akshare as ak
import pandas as pd
from cache_manager import SQLiteCacheManager
from datetime import datetime
import time

def get_hs300_stock_info():
    """
    获取沪深300成分股信息并缓存到本地数据库
    
    Returns:
        bool: 缓存是否成功
    """
    try:
        # 初始化缓存管理器
        cache_manager = SQLiteCacheManager("hs300_stock_cache.db")
        
        print("开始获取沪深300成分股列表...")
        
        # 获取沪深300成分股列表
        hs300_stocks = ak.index_stock_cons(symbol="000300")
        print(f"获取到 {len(hs300_stocks)} 只沪深300成分股")
        
        # 获取每只股票的详细信息
        stock_info_list = []
        failed_stocks = []
        
        # 获取股票实时行情数据
        realtime_data = ak.stock_zh_a_spot_em()

        for index, row in hs300_stocks.iterrows():
            stock_code = row['品种代码']
            stock_name = row['品种名称']
            
            try:
                print(f"正在获取 {stock_code} - {stock_name} 的详细信息... ({index+1}/{len(hs300_stocks)})")
                
                stock_realtime = realtime_data[realtime_data['代码'] == stock_code]
                
                if not stock_realtime.empty:
                    stock_info = stock_realtime.iloc[0].to_dict()
                    
                    # 获取股票基本信息
                    try:
                        stock_individual = ak.stock_individual_info_em(symbol=stock_code)
                        if not stock_individual.empty:
                            # 合并基本信息
                            for _, info_row in stock_individual.iterrows():
                                key = info_row['item']
                                value = info_row['value']
                                if key == '行业':
                                    stock_info['行业'] = value
                                elif key == '上市时间':
                                    stock_info['上市日期'] = value
                                elif key == '总股本':
                                    stock_info['总股本'] = value
                                elif key == '流通股':
                                    stock_info['流通股本'] = value
                    except Exception as e:
                        print(f"获取 {stock_code} 基本信息失败: {e}")
                    
                    stock_info_list.append(stock_info)
                else:
                    print(f"未找到 {stock_code} 的实时数据")
                    failed_stocks.append(f"{stock_code}-{stock_name}")
                
                # 避免请求过于频繁
                time.sleep(0.1)
                
            except Exception as e:
                print(f"获取 {stock_code} - {stock_name} 信息失败: {e}")
                failed_stocks.append(f"{stock_code}-{stock_name}")
                continue
        
        # 批量缓存股票信息
        if stock_info_list:
            print(f"\n开始批量缓存 {len(stock_info_list)} 只股票信息...")
            cache_manager.batch_cache_stock_info(stock_info_list)
            
            # 显示缓存统计
            stats = cache_manager.get_cache_stats()
            print(f"\n缓存统计:")
            print(f"- 总股票数: {stats['total_stocks']}")
            print(f"- 数据库大小: {stats['db_size_mb']} MB")
            
            if failed_stocks:
                print(f"\n失败的股票 ({len(failed_stocks)} 只):")
                for failed in failed_stocks:
                    print(f"- {failed}")
            
            return True
        else:
            print("未获取到任何股票信息")
            return False
            
    except Exception as e:
        print(f"获取沪深300股票信息失败: {e}")
        return False

def get_zz2000_stock_info():
    """
    获取中证2000成分股信息并缓存到本地数据库

    Returns:
        bool: 缓存是否成功
    """
    try:
        # 初始化缓存管理器
        cache_manager = SQLiteCacheManager("zz2000_stock_cache.db")
        
        print("开始获取中证2000成分股列表...")
        
        # 获取中证2000成分股列表
        zz2000_stocks = ak.index_stock_cons(symbol="000932")
        print(f"获取到 {len(zz2000_stocks)} 只中证2000成分股")
        
        # 获取每只股票的详细信息
        stock_info_list = []
        failed_stocks = []
        
        # 获取股票实时行情数据
        realtime_data = ak.stock_zh_a_spot_em()

        for index, row in zz2000_stocks.iterrows():
            stock_code = row['品种代码']
            stock_name = row['品种名称']
            
            try:
                print(f"正在获取 {stock_code} - {stock_name} 的详细信息... ({index+1}/{len(zz2000_stocks)})")
                
                stock_realtime = realtime_data[realtime_data['代码'] == stock_code]
                
                if not stock_realtime.empty:
                    stock_info = stock_realtime.iloc[0].to_dict()
                    
                    # 获取股票基本信息
                    try:
                        stock_individual = ak.stock_individual_info_em(symbol=stock_code)
                        if not stock_individual.empty:
                            for _, info_row in stock_individual.iterrows():
                                key = info_row['item']
                                value = info_row['value']
                                if key == '行业':
                                    stock_info['行业'] = value
                                elif key == '上市时间':
                                    stock_info['上市日期'] = value
                                elif key == '总股本':
                                    stock_info['总股本'] = value
                                elif key == '流通股':
                                    stock_info['流通股本'] = value
                    except Exception as e:
                        print(f"获取 {stock_code} 基本信息失败: {e}")
                    
                    stock_info_list.append(stock_info)
                else:
                    print(f"未找到 {stock_code} 的实时数据")
                    failed_stocks.append(f"{stock_code}-{stock_name}")
                
                time.sleep(0.1)
                
            except Exception as e:
                print(f"获取 {stock_code} - {stock_name} 信息失败: {e}")
                failed_stocks.append(f"{stock_code}-{stock_name}")
                continue
        
        # 批量缓存股票信息
        if stock_info_list:
            print(f"\n开始批量缓存 {len(stock_info_list)} 只股票信息...")
            cache_manager.batch_cache_stock_info(stock_info_list)
            
            # 显示缓存统计
            stats = cache_manager.get_cache_stats()
            print(f"\n缓存统计:")
            print(f"- 总股票数: {stats['total_stocks']}")
            print(f"- 数据库大小: {stats['db_size_mb']} MB")
            
            if failed_stocks:
                print(f"\n失败的股票 ({len(failed_stocks)} 只):")
                for failed in failed_stocks:
                    print(f"- {failed}")
            
            return True
        else:
            print("未获取到任何股票信息")
            return False
            
    except Exception as e:
        print(f"获取中证2000股票信息失败: {e}")
        return False

def query_cached_stocks():
    """
    查询已缓存的股票信息示例
    """
    cache_manager = SQLiteCacheManager("hs300_stock_cache.db")
    
    print("\n=== 缓存查询示例 ===")
    
    # 查询特定股票
    stock_info = cache_manager.get_stock_info("000001")
    if stock_info:
        print(f"\n平安银行 (000001) 信息:")
        print(f"- 股票名称: {stock_info.get('stock_name', 'N/A')}")
        print(f"- 所属行业: {stock_info.get('industry', 'N/A')}")
        print(f"- 收盘价: {stock_info.get('close_price', 'N/A')}")
        print(f"- 市值: {stock_info.get('market_cap', 'N/A')}")
        print(f"- 换手率: {stock_info.get('turnover_rate', 'N/A')}%")
    
    # 查询银行行业股票
    bank_stocks = cache_manager.get_stocks_by_industry("银行")
    if bank_stocks:
        print(f"\n银行行业股票 ({len(bank_stocks)} 只):")
        for stock in bank_stocks[:5]:  # 只显示前5只
            print(f"- {stock.get('stock_code', 'N/A')} {stock.get('stock_name', 'N/A')} - 市值: {stock.get('market_cap', 'N/A')}")

def update_stock_prices():
    """
    更新已缓存股票的最新价格信息
    """
    cache_manager = SQLiteCacheManager("hs300_stock_cache.db")
    
    try:
        print("\n=== 更新股票价格 ===")
        
        # 获取实时行情数据
        realtime_data = ak.stock_zh_a_spot_em()
        
        # 获取已缓存的股票代码
        stats = cache_manager.get_cache_stats()
        if stats['total_stocks'] == 0:
            print("数据库中没有缓存的股票信息")
            return
        
        print(f"开始更新 {stats['total_stocks']} 只股票的价格信息...")
        
        updated_count = 0
        for _, row in realtime_data.iterrows():
            stock_code = row['代码']
            
            # 检查是否已缓存该股票
            existing_stock = cache_manager.get_stock_info(stock_code)
            if existing_stock:
                # 更新价格信息
                updated_data = row.to_dict()
                cache_manager.cache_stock_info(updated_data)
                updated_count += 1
        
        print(f"成功更新 {updated_count} 只股票的价格信息")
        
    except Exception as e:
        print(f"更新股票价格失败: {e}")

if __name__ == "__main__":
    print("=== 股票信息缓存工具 ===")
    print("1. 获取并缓存沪深300股票信息")
    print("2. 查询已缓存的股票信息")
    print("3. 更新股票价格")
    print("4. 获取并缓存中证2000股票信息")  # 新增

    choice = input("\n请选择操作 (1/2/3/4): ").strip()
    
    if choice == "1":
        print("\n开始获取沪深300股票信息...")
        success = get_hs300_stock_info()
        if success:
            print("\n✅ 沪深300股票信息缓存完成!")
        else:
            print("\n❌ 缓存失败，请检查网络连接和API状态")
    
    elif choice == "2":
        query_cached_stocks()
    
    elif choice == "3":
        update_stock_prices()

    elif choice == "4":
        print("\n开始获取中证2000股票信息...")
        success = get_zz2000_stock_info()
        if success:
            print("\n✅ 中证2000股票信息缓存完成!")
        else:
            print("\n❌ 缓存失败，请检查网络连接和API状态")
    else:
        print("无效选择")