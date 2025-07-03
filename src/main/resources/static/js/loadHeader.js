// header.html load
fetch('header.html')
  .then(response => response.text())
  .then(data => {
    document.getElementById('header-load').innerHTML = data;
  })
  .catch(error => console.error('Error loading header:', error));
