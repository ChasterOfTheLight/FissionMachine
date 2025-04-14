import tkinter as tk
from tkinter import ttk
from itertools import product

class FootballScorePredicition:
    def __init__(self, root):
        self.root = root
        self.root.title("足球比分计算器")
        
        # 创建进球范围输入框
        tk.Label(root, text="最小进球数:").grid(row=0, column=0, padx=5, pady=5)
        self.min_goals = tk.Entry(root)
        self.min_goals.grid(row=0, column=1, padx=5, pady=5)
        self.min_goals.insert(0, "0")
        
        tk.Label(root, text="最大进球数:").grid(row=1, column=0, padx=5, pady=5)
        self.max_goals = tk.Entry(root)
        self.max_goals.grid(row=1, column=1, padx=5, pady=5)
        self.max_goals.insert(0, "4")
        
        # 盘口输入
        tk.Label(root, text="盘口(如: 2.5):").grid(row=2, column=0, padx=5, pady=5)
        self.handicap = tk.Entry(root)
        self.handicap.grid(row=2, column=1, padx=5, pady=5)
        
        # 胜平负赔率输入
        tk.Label(root, text="主胜赔率:").grid(row=3, column=0, padx=5, pady=5)
        self.win_odds = tk.Entry(root)
        self.win_odds.grid(row=3, column=1, padx=5, pady=5)
        
        tk.Label(root, text="平局赔率:").grid(row=4, column=0, padx=5, pady=5)
        self.draw_odds = tk.Entry(root)
        self.draw_odds.grid(row=4, column=1, padx=5, pady=5)
        
        tk.Label(root, text="客胜赔率:").grid(row=5, column=0, padx=5, pady=5)
        self.lose_odds = tk.Entry(root)
        self.lose_odds.grid(row=5, column=1, padx=5, pady=5)
        
        # 创建计算按钮
        tk.Button(root, text="生成可能比分", command=self.calculate).grid(row=6, column=0, columnspan=2, pady=10)
        
        # 创建结果显示区域
        self.result_text = tk.Text(root, height=15, width=50)
        self.result_text.grid(row=7, column=0, columnspan=2, padx=5, pady=5)
        
    def calculate(self):
        try:
            min_goals = int(self.min_goals.get())
            max_goals = int(self.max_goals.get())
            handicap = float(self.handicap.get())
            win_odds = float(self.win_odds.get()) if self.win_odds.get() else 0
            draw_odds = float(self.draw_odds.get()) if self.draw_odds.get() else 0
            lose_odds = float(self.lose_odds.get()) if self.lose_odds.get() else 0
            
            # 生成所有可能的比分组合
            possible_scores = []
            for total_goals in range(min_goals, max_goals + 1):
                for home in range(total_goals + 1):
                    for away in range(total_goals + 1):
                        if home + away == total_goals:
                            possible_scores.append((home, away))
            
            # 清空结果显示区域
            self.result_text.delete(1.0, tk.END)
            
            # 显示结果
            self.result_text.insert(tk.END, f"可能的比分组合：\n")
            self.result_text.insert(tk.END, f"{'比分':<8}{'总进球':<8}{'赛果':<6}{'盘路':<6}{'对应赔率'}\n")
            self.result_text.insert(tk.END, "-" * 40 + "\n")
            
            for home, away in possible_scores:
                total = home + away
                diff = home - away
                
                if diff > 0:
                    result = "主胜"
                    odds = win_odds
                elif diff < 0:
                    result = "客胜"
                    odds = lose_odds
                else:
                    result = "平局"
                    odds = draw_odds
                
                handicap_result = "赢盘" if diff > handicap else "输盘" if diff < handicap else "走水"
                
                self.result_text.insert(tk.END, 
                    f"{home}-{away:<6}{total:<8}{result:<6}{handicap_result:<6}{odds:.2f}\n")
                
        except ValueError:
            self.result_text.delete(1.0, tk.END)
            self.result_text.insert(tk.END, "请输入有效的数字！")

if __name__ == "__main__":
    root = tk.Tk()
    app = FootballScorePredicition(root)
    root.mainloop()