-- Add description column to BOOKS table
ALTER TABLE BOOKS ADD DESCRIPTION VARCHAR(2000);

-- Update existing books with sample descriptions (optional)
UPDATE BOOKS SET DESCRIPTION = 'No description available.' WHERE DESCRIPTION IS NULL; 