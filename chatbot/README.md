# VectorDB - Build a Vector Database from Scratch in C++

A C++ implementation of a Vector Database that demonstrates how modern semantic search engines work internally. The project supports multiple nearest neighbor search algorithms, vector similarity metrics, and a Retrieval-Augmented Generation (RAG) pipeline using Ollama.

---

## What This Project Does

- Stores and manages high-dimensional vector embeddings.
- Performs semantic search using **HNSW**, **KD-Tree**, and **Brute Force** algorithms.
- Supports **Cosine Similarity**, **Euclidean Distance**, and **Manhattan Distance**.
- Provides REST APIs for inserting, deleting, and searching vectors.
- Converts text into embeddings using **Ollama**.
- Implements a complete **RAG (Retrieval-Augmented Generation)** pipeline for document-based question answering.

---

## How It Works

```text
User Text
    │
    ▼
Ollama Embedding Model
    │
    ▼
768-Dimensional Vector
    │
    ▼
Stored in Vector Database (HNSW Index)
    │
    ▼
Semantic Search
    │
    ▼
Top Matching Documents
    │
    ▼
Llama 3.2
    │
    ▼
Generated Response
```

### Working Model

1. The user enters text or uploads a document.
2. Ollama converts the text into a high-dimensional vector embedding.
3. The vector is stored inside the database using the HNSW index.
4. When the user searches, the query is converted into another embedding.
5. The selected search algorithm compares the query vector with stored vectors and retrieves the nearest matches.
6. For RAG, the retrieved document chunks are provided as context to the language model.
7. The language model generates a response based on the retrieved information.

---

## Search Algorithms

### HNSW (Hierarchical Navigable Small World)
Uses a multi-layer graph structure to perform approximate nearest neighbor search efficiently. It provides very fast search performance for high-dimensional vectors.

### KD-Tree
Partitions the vector space into multiple regions, allowing faster exact searches for low-dimensional datasets.

### Brute Force
Compares the query vector with every stored vector to return the exact nearest neighbors. It is simple but slower for large datasets.

---

## Project Architecture

```text
             User Input
                 │
                 ▼
        Ollama Embedding Model
                 │
                 ▼
        Vector Embedding (768D)
                 │
                 ▼
          Vector Database
      ┌─────────┼─────────┐
      │         │         │
    HNSW     KD-Tree   Brute Force
      │         │         │
      └─────────┼─────────┘
                │
                ▼
      Top Matching Vectors
                │
                ▼
        Llama 3.2 (RAG)
                │
                ▼
        Generated Response
```

---

## Summary

This project demonstrates the internal working of modern vector databases by implementing vector storage, similarity search, and Retrieval-Augmented Generation (RAG) completely from scratch in C++. It helps understand how semantic search systems retrieve relevant information efficiently before passing it to a language model to generate accurate responses.
