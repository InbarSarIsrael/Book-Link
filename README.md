# 📚 BookLink – Your Personalized Book Shelf

BookLink is an Android application that allows users to add, view, rate, and comment on books. Built with Kotlin and Firebase,
the app provides a clean interface for managing personal reading experiences and discovering books from other users.

## ✨ Features

- 🔍 **Browse Books** – Explore all books added by users.
- ➕ **Add a Book** – Upload new books with title, author, genres, summary, image, and more.
- ❤️ **Favorites** – Mark books as favorites and view them in your profile.
- ⭐ **Ratings** – Rate books and see the average rating from all users.
- 💬 **Comments** – Leave reviews and read what others say.
- 👤 **User Profile** – View your added books and manage them.
- 🔐 **Authentication** – Secure login with Firebase Authentication.
- ✏️ **Edit Book** – Update your added books directly from your profile.

## 🧰 Tech Stack

- **Language**: Kotlin
- **Framework**: Android SDK
- **Architecture**: MVVM
- **Database**: Firebase Realtime Database
- **Storage**: Firebase Storage
- **Libraries**:
    - Glide (Image loading)
    - ViewBinding
    - Material Components

## 🚀 Getting Started

To run this project:

1. Clone the repository
2. Open in Android Studio
3. Connect to your Firebase project
4. Replace `google-services.json` with your own
5. Run the app on emulator/device

## 🔐 Firebase Setup

Make sure to enable the following in your Firebase project:

- 🔐 Authentication (Email/Google)
- 💾 Realtime Database
- 📦 Storage (for book cover images)

## 📌 Notes

- Image URLs are saved in the `poster` field of each book object.
- Ratings are averaged live via a `ratings/{bookId}/{userId}` structure.
- Comments are stored under `comments/{bookId}`.

## 🧑‍💻 Developed By

> Inbar Sar Israel