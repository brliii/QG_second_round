const axios = require('axios');
const fs = require('fs');
const FormData = require('form-data');

async function testUpload() {
  try {
    const formData = new FormData();
    formData.append('file', fs.createReadStream('./frontend/public/favicon.ico'));

    const response = await axios.post('http://localhost:8080/upload/image', formData, {
      headers: {
        'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJpYXQiOjE3MTUyMDM1MjIsImV4cCI6MTcxNTgwODMyMn0.S_4c8eXp3q2rZ7a1bC9dE0fG5hI6jK7lM8nO9pQ0rS1',
        ...formData.getHeaders()
      }
    });

    console.log('上传成功:', response.data);
  } catch (error) {
    console.error('上传失败:', error.response ? error.response.data : error.message);
  }
}

testUpload();