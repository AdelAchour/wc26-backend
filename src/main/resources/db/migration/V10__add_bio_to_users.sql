-- Migration to add nullable bio column to users table
ALTER TABLE users ADD COLUMN bio VARCHAR(100);