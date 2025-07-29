-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create profiles table
CREATE TABLE profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) UNIQUE NOT NULL
);

-- Create user_profiles junction table
CREATE TABLE user_profiles (
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, profile_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
);

-- Create courses table
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    categoria VARCHAR(50) NOT NULL
);

-- Create topics table
CREATE TABLE topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    mensagem TEXT NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('OPEN', 'CLOSED') DEFAULT 'OPEN',
    autor_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    FOREIGN KEY (autor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (curso_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_topic (titulo, mensagem)
);

-- Create responses table
CREATE TABLE responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mensagem TEXT NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    solucao BOOLEAN DEFAULT FALSE,
    topico_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    FOREIGN KEY (topico_id) REFERENCES topics(id) ON DELETE CASCADE,
    FOREIGN KEY (autor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_topics_data_criacao ON topics(data_criacao DESC);
CREATE INDEX idx_topics_status ON topics(status);
CREATE INDEX idx_responses_topico_id ON responses(topico_id);
CREATE INDEX idx_responses_data_criacao ON responses(data_criacao);