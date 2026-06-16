# Judson's ePortfolio


## CS 499 Computer Science Capstone



### Professional Self-Assessment

Throughout my time as Southern New Hampshire University's Computer Science program, I have developed skills in software engineering, database design, algorithms and data structures, object-oriented programming, testing, debugging, secure coding practices, and project management. These experiences have strengthened my ability to design, implement, and maintain software applications while applying and adhering to the best industry standards and practices.
This ePortfolio demonstrates my growth as a software developer through the enhancement of an Android application that I created towards the beginning of my studies. This project showcases my ability to analyze existing code, identify opportunities for improvement, implement enhancements, and document the development process. The completed project highlights competencies in software design and engineering, algorithms and data structures, and database development.



## Capstone Project Overview
### Judson's Multimedia Library

The artifact selected for this capstone project is an Android multimedia library application that I built from the ground up, starting with a simple inventory management app originally created in CS 360: Mobile Architecture and Programming. Over the course of CS 499, I transformed the app into a fully realized, multi-category media library that allows users to catalog books, movies, and video games with secure authentication efficient in-memory sorting and searching, and a normalized relational database back end. The app allows users to create accounts, manage personal collections of media, and stores information on them, such as titles, release years, ratings, and notes. 
For this capstone, the original artifact was enhanced to improve usability, maintainability, data management, and overall software quality by using Java for Android and relational database design using SQLite. This project also helped reinforce the fundamentals, such as writing clear code comments, documenting design decisions, and producing professional narratives that explain technical work to a wide range of audiences. These enhancements demonstrate achievement of Computer Science program outcomes through practical application of software engineering principles and fundamentals.



## Code Review

A comprehensive code review of the original CS 360 inventory app was the first step. It was conducted before beginning the enhancement process. The code review examined the existing architecture, database implementation, user interface design, and functionality to identify weaknesses, security vulnerabilities, and areas of improvement that were applicable for the three enhancement categories - software design and engineering, algorithms and data structures, and databases. It outlined the planned enhancements and connected them to the CS 499 course outcomes.
### Code Review Video
**YouTube Link** for the **[Code Review](https://www.youtube.com/watch?v=INHdbpCs2wI)**



## **Category One Enhancement**: Software Design and Engineering
### Artifact Description

The artifact for this category, and all other categories, is the Android media library application. This originates from the final project from CS 360: Mobile Architecture and Programming. In its original form, the app displayed inventory items in a single screen GridLayout, and it listed the items in the order that they were added. It displayed the name of the item, quantity, and allowed the user to delete the item from the list. The app was functional but contained several limitations related to user experience, navigation, maintainability, scalability, and overall organization.

### Enhancements Completed
* Redesigned portions of the user interface for improved usability.
* Improved navigation between application screens and fragments using a BottomNavigationView.
* Updated the entire visual design and refactored code to improve readability and maintainability.
* Added a consistent, lighter pastel color  theme throughout.
* Added input validation to prevent invalid user entries.
* Enhanced error handling and application stability.
* Improved commenting and documentation throughout the code base.
* Replaced the GridLayout with a RecyclerView, backed by a custom MediaItemAdapter using the ViewHolder pattern.
* Expanded app from a single generic inventory into a three category media library that supports books, movies, and games.

### Course Outcomes Met
This enhancement demonstrates the fourth course outcome: the ability to use well-founded and innovative techniques, skills, and tools in computing practices for the purpose of implementing computer solutions that deliver value and accomplish industry-specific goals.

### Narrative
[Read the full Category One Narrative](https://github.com/CatNoBanana/CS-499---Library-App/blob/main/Category%20One%20Narrative.pdf)
 
 
 
## **Category Two Enhancement**: Algorithms and Data Structures
### Artifact Description

The original artifact relied on simple data retrieval and refreshed the entire GridLayout if there was any change in data. The app had no algorithmic design, as items were just loaded from the database in insertion order with no sorting, searching, or in-memory organization of any kind.

### Enhancements Completed
* Improved data retrieval and organization processes.
* Enhanced collection management functionality.
* Optimized list loading and display operations.
* Improved handling of category-based filtering.
* Refined data processing logic to increase efficiency and maintainability.
* HashMap-based category indexing.
* Merge Sort to display media items based on title (A->Z or Z->A), year (newest or oldest first), or rating (highest or lowest first).
* Binary search used to search for media items in alphabetical order.

### Course Outcomes Met
This enhancement directly addresses the third course outcome: designing and evaluating computing solutions that solve a given problem using algorithmic principles and computer science practices and standards appropriate to its solution, while managing the trade-offs involved in design choices.

### Narrative
[Read the full Category Two Narrative](https://github.com/CatNoBanana/CS-499---Library-App/blob/main/Category%20Two%20Narrative.pdf)



## **Category Three Enhancement**: Databases
### Artifact Description

The original artifact utilized an SQLite database to store user accounts and item information. It was a two-table SQLite schema with a simple users table to house 








