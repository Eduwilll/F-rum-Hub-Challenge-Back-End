-- Insert more courses for better variety
INSERT INTO courses (nome, categoria) VALUES 
('JavaScript Fundamentals', 'Frontend'),
('Python for Data Science', 'Data Science'),
('Node.js', 'Backend'),
('Vue.js', 'Frontend'),
('PostgreSQL', 'Database'),
('Kubernetes', 'DevOps'),
('AWS Fundamentals', 'Cloud'),
('Machine Learning', 'Data Science'),
('GraphQL', 'Backend'),
('TypeScript', 'Frontend'),
('MongoDB', 'Database'),
('Jenkins', 'DevOps'),
('Redis', 'Database'),
('Elasticsearch', 'Database'),
('RabbitMQ', 'Backend');

-- Create sample admin user (password: admin123)
INSERT INTO users (nome, email, senha, created_at) VALUES 
('Administrador', 'admin@forum.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqyPw5eee2hAzs1w8CRvQaa', NOW());

-- Create sample moderator user (password: mod123)
INSERT INTO users (nome, email, senha, created_at) VALUES 
('Moderador Forum', 'moderador@forum.com', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', NOW());

-- Assign admin role to admin user
INSERT INTO user_profiles (user_id, profile_id) 
SELECT u.id, p.id 
FROM users u, profiles p 
WHERE u.email = 'admin@forum.com' AND p.nome = 'ADMIN';

-- Assign moderator role to moderator user
INSERT INTO user_profiles (user_id, profile_id) 
SELECT u.id, p.id 
FROM users u, profiles p 
WHERE u.email = 'moderador@forum.com' AND p.nome = 'MODERADOR';

-- Add additional indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_topics_autor_id ON topics(autor_id);
CREATE INDEX idx_topics_curso_id ON topics(curso_id);
CREATE INDEX idx_responses_autor_id ON responses(autor_id);
CREATE INDEX idx_responses_solucao ON responses(solucao);
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_profile_id ON user_profiles(profile_id);