# 英文演示讲解稿 — Campus Visitor Management System

> **用法说明**
> - 正文英文是**口播内容**，直接照着读即可；【操作】行是中文动作提示，不用读出来。
> - 全文约 800 词。慢速朗读约 **8～9 分钟**，留有缓冲，控制在 10 分钟以内。
> - 建议演示时把界面语言切成 **English**（右上角语言按钮），这样你读的英文和屏幕上的文字一致，老师也能对上。
> - 标注「可跳过」的部分如果时间紧张就直接跳过。

---

## 1. 开场（0:00 – 0:30）

**【操作】** 站在标题页/首页，微笑，语速放慢

> Hello, teacher. My name is [你的名字]. Today I will present my project: the **Campus Visitor Management System**. It is a web system for campus visitor appointments, review, and gate control. Please let me show you how it works.

---

## 2. 系统概览（0:30 – 1:20）

**【操作】** 指向页面 / 幻灯片

> This system has two sides.
>
> One side is for **visitors**. Visitors do not need an account. They open the website and make an appointment.
>
> The other side is for **admins**. Admins log in, review appointments, manage visitors, and check gate records.
>
> The system supports two languages: Chinese and English. I will use English now. 【操作：点击右上角语言按钮切换】
>
> About the technology: the backend uses **Java Spring Boot** and **MySQL**. The frontend uses **Vue 3**. The system also uses **DeepSeek AI** to review appointments automatically.

---

## 3. 访客端：在线预约（1:20 – 2:30）

**【操作】** 打开预约页 http://localhost:3000/apply

> Now let's start from the visitor side. This is the **appointment page**. The visitor fills in a simple form: name, phone number, ID number, the person to visit, the visit time, and the purpose.
>
> Look at the purpose list. We have many common reasons, like **Campus Tour** or **Job Interview**. There is also an **"Other"** option. If the visitor chooses Other, they must write the specific reason here. 【操作：选中 Other，指一下出现的输入框】
>
> This detail is very important, because the **AI** will read it later.
>
> Now I click **Submit**. 【操作：填写并提交一份预约】
>
> The system says: "Appointment submitted successfully. Pending review." Good.

---

## 4. 访客端：结果查询（2:30 – 3:10）「可跳过」

**【操作】** 打开查询页 http://localhost:3000/query

> Next, the visitor wants to know the result. This is the **lookup page**. The visitor enters the phone number. 【操作：输入刚才的手机号，点 Search】
>
> Here is the record. We can see the **status**: Pending Review, Approved, or Rejected. If it is rejected, the reason is shown in this column. Very easy for visitors.

---

## 5. 管理端：登录（3:10 – 3:40）

**【操作】** 打开登录页，输入 admin / admin123

> Now let's look at the **admin side**. This is the login page. I use the admin account. 【操作：点击 Login】
>
> The system uses **JWT tokens** for security. Every request is checked. Different roles have different permissions.

---

## 6. 首页概览（3:40 – 4:50）

**【操作】** 进入 Dashboard

> This is the **dashboard**. The four cards show the key numbers: **today's visitors**, **visitors currently on campus**, **monthly visitors**, and **pending approvals**.
>
> Below are three charts. The **bar chart** shows visitor traffic for the last 7 days, 4 weeks, or 12 months. 【操作：切换一下时间范围】The **line chart** shows the traffic by hour. The **pie chart** shows the purpose distribution. One small detail: all "Other" reasons are grouped into one slice, so the chart stays clean.
>
> All the data comes from the database in real time.

---

## 7. AI 自动审批（4:50 – 6:50）★ 重点部分

**【操作】** 点击左侧菜单 AI Review Settings

> This is my favorite part. Let's open **AI Review Settings**. Here is the **AI switch**. When it is ON, every new appointment is sent to **DeepSeek AI** automatically. 【操作：打开开关并保存】
>
> The AI reads the visitor's purpose, the host, and the visit time, and then decides: **approve or reject**. It also gives a **confidence** score.
>
> Privacy is very important. The system does **NOT** send the ID card number to the AI, and the phone number is **masked**. So personal data is protected.
>
> Down here is the **AI log**. Every AI call is recorded: the decision, the reason, the confidence, the latency, and the error message if it failed.
>
> Now let's test it. 【操作：回到 /apply，再提交一份新预约，然后回到审批页刷新】
>
> Look — the new appointment is already reviewed. The status is decided, and we did nothing. No human work needed. This saves a lot of time for campus staff.
>
> Of course, admins can still review **manually**. Here I can approve or reject any appointment by hand. 【操作：点一下 Approve 或 Reject 按钮示意】

---

## 8. 访客管理与黑名单（6:50 – 7:30）

**【操作】** 点击左侧菜单 Visitor List

> Next is **Visitor Management**. This is the visitor list. I can search by name or phone.
>
> Here is an important feature: the **blacklist**. If a visitor breaks the rules, I can add them to the blacklist with one click. 【操作：点 Add to Blacklist】
>
> Then the gate will deny this visitor, and the system will log it.

---

## 9. 门禁管理（7:30 – 8:30）

**【操作】** 点击左侧菜单 Gate Records / Access Control

> Finally, **Access Control**. This page is for the guard at the gate. To register an entry, the guard enters the visitor's phone number, and the system finds the visitor and the approved appointment. 【操作：输入手机号，登记入校】
>
> If the visitor is on the **blacklist**, a red warning appears: "This visitor is blacklisted. Access will be denied and logged."
>
> There is also an **overstay alert**: if a visitor stays too long, the system warns the staff.
>
> The table at the bottom shows all gate records, with entry time, exit time, and status. So we always know who is on campus.

---

## 10. 结尾（8:30 – 9:00）

**【操作】** 回到首页概览，收尾

> That is all for the system. To summarize: visitors can apply online in one minute; AI can review appointments automatically, 24 hours a day; and admins can manage visitors, approvals, gates, and statistics in one place.
>
> Thank you for listening. I am happy to answer your questions.

---

## 应急句（出错时用）

- **Sorry, let me try again.**（抱歉，我再试一次）
- **One moment, please.**（请稍等）
- **Let me show you this part again.**（我再演示一遍这部分）
- **As you can see here...**（如你所见……）—— 万一张嘴卡住，用手指屏幕 + 这句过渡

## 难词发音提示

| 单词 | 读法 |
|---|---|
| appointment | 呃-**破因特**-ment（重音在第二音节） |
| purpose | **破**-pəs |
| automatically | 哦-特-麦-提克-li |
| statistics | 斯特-提斯-提克斯 |
| distribution | 迪斯-吹-**比**-shən |
| confidence | **康**-fɪ-dəns |
| latency | **累**-tən-si |
| privacy | **扑来**-və-si |
| guest / gate | guest = 盖斯特；gate = 给特 |
| JWT | 读字母：J-W-T |
| DeepSeek | Deep-Seek（两段都读清楚） |

## 演示前检查清单（重要）

1. **MySQL 服务已启动**：管理员命令行执行 `net start MySQL96`（若已 RUNNING 则跳过）
2. **后端已启动**：`cd visitor-backend && ./mvnw spring-boot:run`（等待 8080 端口就绪）
3. **前端已启动**：`cd visitor-frontend && npm run dev` → 打开 http://localhost:3000
4. **AI 可用性提前验证**（关键！）：
   - 后端环境变量 `DEEPSEEK_API_KEY` 已配置
   - 已在设置页打开 AI 开关并保存
   - **提前试提交一份预约**，确认 AI 能正常返回审批结果（避免现场调用超时）
5. **浏览器准备**：提前开好两个标签页 —— 访客预约页 `/apply` 和管理端首页；管理端提前登录（admin / admin123），避免现场输入慢
6. **界面语言切到 English**：右上角语言按钮
7. **演示数据充足**：默认种子数据含 12 位访客和多条预约，图表不为空即可
