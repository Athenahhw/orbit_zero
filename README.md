
# VOID VECTOR: Solar Odyssey 🚀

> **A procedural space shooter built from scratch using Java 2D API.** > 一款完全使用 Java 原生圖形庫打造的物理慣性射擊遊戲。

## 📖 Introduction (專案簡介)

**VOID VECTOR** is a vertical scrolling shooter (STG) that simulates Newtonian physics in a 2D environment. Unlike traditional arcade shooters, the spaceship in this game possesses **inertia and momentum**, requiring players to master the controls of thrust and rotation.

The game features a complete solar system journey, from Neptune to the Sun, with dynamic difficulty scaling, branching storylines (Earth vs. Moon), and a custom particle system.

這是一個模擬牛頓物理慣性的縱向捲軸射擊遊戲。不同於傳統街機遊戲，本作的飛船具有**慣性與動量**，玩家需要精確控制推進器與轉向。遊戲包含完整的太陽系關卡、動態難度調整以及分支路線系統。

## ✨ Key Features (核心特色)

* **🕹️ Physics-Based Movement**: Implements acceleration, deceleration, and angular momentum for realistic zero-gravity handling.
* (實作加速度與角動量，模擬真實的無重力操控感)


* **🎨 Code-Generated Graphics**: No external sprites used. All assets (ships, asteroids, particles) are drawn in real-time using `java.awt.Graphics2D` geometry.
* (不使用外部貼圖，所有畫面皆由程式碼幾何運算即時繪製)


* **🤖 Smart Enemy AI**: Enemies track player position, calculate intercept angles, and adjust engagement speed based on difficulty levels.
* (敵人具備追蹤與攔截算法，並隨難度動態調整行為)


* **🌌 Dynamic Particle System**: Custom engine for rendering explosions, engine exhaust, and starfields with alpha blending.
* (自製粒子系統，處理爆炸、尾焰與星空背景的透明度混合)


* **🗺️ Branching Level Design**: Unique "Earth Choice" mechanic allowing players to choose their path (Moon Base or Earth Re-entry).
* (分支路線設計，玩家可選擇前往月球或返回地球)



## 🛠️ Technical Highlights (技術細節)

This project demonstrates proficiency in **Object-Oriented Programming (OOP)** and **Computational Geometry**:

* **Polymorphism**: Unified entity management for `Asteroid`, `Bullet`, and `Enemy` objects.
* (運用多型統一管理所有遊戲物件)


* **Vector Math**: Used for calculating trajectories, rotation matrices, and collision detection (`Math.sin`, `Math.cos`, `Math.atan2`).
* (大量運用向量數學計算軌跡、旋轉矩陣與碰撞判定)


* **Audio Synthesis**: Sound effects are generated programmatically by manipulating byte arrays of sine waves, removing dependencies on audio files.
* (程式化音效合成，直接操作 Byte Array 生成正弦波音效)


* **State Management**: Finite State Machine (FSM) controlling Game Menu, Playing, Victory, and Game Over states.
* (使用有限狀態機管理遊戲流程)



## 🎮 How to Play (操作說明)

| Key | Action |
| --- | --- |
| **↑ (Up Arrow)** | Thrust / Accelerate (推進) |
| **↓ (Down Arrow)** | Brake / Decelerate (減速) |
| **← / →** | Rotate Ship (旋轉機身) |
| **SPACE** | Fire Photon Torpedoes (射擊) |
| **ENTER** | Start / Next Level (開始/下一關) |

## 🚀 Installation & Run (安裝與執行)

Make sure you have **Java Development Kit (JDK) 8** or higher installed.

1. **Clone the repository**
```bash
git clone https://github.com/YourUsername/Void-Vector.git
cd Void-Vector

```


2. **Compile the code**
```bash
javac airplane_war.java

```


3. **Run the game**
```bash
java airplane_war

```



## 📸 Screenshots (遊戲截圖)

*(Please replace these placeholder links with actual screenshots of your game!)*

| Main Menu | Combat Scene | Level Complete |
| --- | --- | --- |
|  |  |  |

## 👥 Authors (開發團隊)

**Team Binary Star** (NTUE CS Department)

* **Athena Wang (王暄菡)** - Lead Developer, Physics Engine, Gameplay Logic

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](https://www.google.com/search?q=LICENSE) file for details.
