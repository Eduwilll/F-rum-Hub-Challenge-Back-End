-- Insert default profiles
INSERT INTO profiles (nome) VALUES 
('USUARIO'),
('MODERADOR'),
('ADMIN');

-- Insert sample courses
INSERT INTO courses (nome, categoria) VALUES 
('Spring Boot', 'Backend'),
('Java Fundamentals', 'Backend'),
('React', 'Frontend'),
('Angular', 'Frontend'),
('MySQL', 'Database'),
('Docker', 'DevOps'),
('Microservices', 'Architecture'),
('REST APIs', 'Backend');