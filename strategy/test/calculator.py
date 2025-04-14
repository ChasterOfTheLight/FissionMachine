import tkinter as tk
from tkinter import messagebox

class Calculator:
    def __init__(self, root):
        self.root = root
        self.root.title("简单计算器")
        
        # 创建显示结果的输入框
        self.display = tk.Entry(root, width=30, justify="right")
        self.display.grid(row=0, column=0, columnspan=4, padx=5, pady=5)
        
        # 按钮布局
        buttons = [
            '7', '8', '9', '/',
            '4', '5', '6', '*',
            '1', '2', '3', '-',
            '0', '.', '=', '+'
        ]
        
        # 创建按钮
        row = 1
        col = 0
        for button in buttons:
            cmd = lambda x=button: self.click(x)
            tk.Button(root, text=button, width=7, height=2, command=cmd).grid(row=row, column=col)
            col += 1
            if col > 3:
                col = 0
                row += 1
        
        # 添加清除按钮
        tk.Button(root, text='清除', width=7, height=2, command=self.clear).grid(row=row, column=col)
        
        # 添加创建子窗口的按钮
        self.help_button = tk.Button(root, text="帮助", width=7, height=2, command=self.show_help)
        self.help_button.grid(row=5, column=0)

    def click(self, key):
        if key == '=':
            try:
                result = eval(self.display.get())
                self.display.delete(0, tk.END)
                self.display.insert(tk.END, str(result))
            except:
                messagebox.showerror("错误", "计算表达式错误")
                self.display.delete(0, tk.END)
        else:
            self.display.insert(tk.END, key)

    def clear(self):
        self.display.delete(0, tk.END)
    
    def show_help(self):
        # 创建子窗口
        help_window = tk.Toplevel(self.root)
        help_window.title("帮助信息")
        help_window.geometry("300x200")  # 设置子窗口大小
        
        # 在子窗口中添加内容
        help_text = """使用说明：
1. 点击数字和运算符进行输入
2. 按 = 计算结果
3. 按清除键重新开始"""
        
        label = tk.Label(help_window, text=help_text, justify="left", padx=10, pady=10)
        label.pack()
        
        # 添加关闭按钮
        close_button = tk.Button(help_window, text="关闭", command=help_window.destroy)
        close_button.pack(pady=10)

if __name__ == "__main__":
    root = tk.Tk()  # 修改这里：使用 Tk() 而不是 Window()
    calculator = Calculator(root)
    root.mainloop()