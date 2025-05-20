# 🚀 LeetCodeRevs

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/Database-MongoDB-green.svg)](https://www.mongodb.com/)
[![WebSocket](https://img.shields.io/badge/Notifications-WebSocket-blueviolet.svg)](https://stomp.github.io/)
[![CLI Support](https://img.shields.io/badge/Interface-CLI-orange.svg)]()
[![Languages](https://img.shields.io/badge/Languages-Java%20%7C%20C++%20%7C%20Python-yellow.svg)]()

A powerful personalized DSA revision system that assigns **daily coding questions** based on your performance and topic understanding. Built with real-time **notifications**, **submission support**, and a native **Java CLI interface**, this system ensures consistency, growth, and streak maintenance for LeetCode-style problem-solving.

Supports **Java**, **C++**, and **Python**.

---

## 👤 For Users (CLI)

### ✅ Key Features

- 🔐 Login once — persistent sessions via cookies
- 📮 Automatically assigned questions every 24 hours
    - **Performance-based** (based on your historical weights)
    - **Topic-based** (weaker topic priority)
- 🔃 Automatically fetches **latest LeetCode questions** using internal API
- 📁 Auto-generate code templates with test case stubs
- 💻 Supported Languages: Java, C++, Python
- 🔔 Real-time notification alerts for:
    - New daily assignments
    - Streak reminder if not solved by night
- ✅ CLI Submission Runner *(coming soon)*

### 🧾 Commands

| Command                                | Description                                           |
|----------------------------------------|-------------------------------------------------------|
| `lcr -login`                           | Login and start session                              |
| `lcr -getDailyQues1 --lang java`       | Get performance-based daily assigned question        |
| `lcr -getDailyQues2 --lang cpp`        | Get topic-wise weak area assigned question           |
| `lcr -check`                           | Check if user session is valid                       |
| `lcr -submit`                          | Submit code and check against test cases *(coming soon)* |

---

---

##  Internals & Architecture

###  Assignment Logic

- Every day, two questions are auto-assigned to the user:
    1. **Performance-based assignment**:
        - Uses a weight-based probabilistic algorithm to prioritize weaker questions.
    2. **Topic-based assignment**:
        - Based on the least-performing topic area using past submission metadata.

-  The system **automatically fetches the latest LeetCode questions and metadata** (title, link, tags, difficulty, etc.) using a scraping/parser utility.

- Each question’s weight is updated after every submission depending on:
    - Time taken to solve
    - Number of retries
    - Difficulty

- Question pool avoids repetition until sufficient time has passed or weak weight re-emerges.

---

###  Tech Stack

| Layer        | Tech Used                                      |
|--------------|------------------------------------------------|
| Backend      | Spring Boot, Spring Security, MongoDB          |
| CLI Client   | Java 17, Picocli, HttpClient                   |
| Notifications| STOMP WebSocket (SockJS) + React Toastify      |
| Caching      | Caffeine                                       |
| Submission   | Judge0 API *(planned)*                         |
| LeetCode Sync| Internal Scraper / API Puller for question sync|

---

###  Notifications System

- Real-time alerts are sent using WebSocket & STOMP.
- Users receive:
    - Assignment push every morning
    - Reminder push if not solved by night
- Notifications are shown in-app and via system notifications in frontend.
- CLI version may add push-like terminal alerts in future (TBD).

---

###  Future Plans

- ⚙️ **Online Code Submission Runner** (via Judge0 API)
- 📈 **Graphical dashboard** (track topic progress & time trends)
- 🕒 **Streak tracking system**
- 📦 **Better CLI integration** with a built-in code executor
- 🧠 **Adaptive recommender system** using ML for question difficulty