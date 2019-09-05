document.addEventListener("DOMContentLoaded", function () {
  'use strict';
  document.querySelectorAll('time').forEach(function (time) {
    time.textContent = new Date(time.dateTime).toLocaleString();
  });
});
