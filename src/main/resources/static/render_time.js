document.addEventListener("DOMContentLoaded", function () {
  'use strict';
  document.querySelectorAll('time').forEach(function (time) {
    var date = new Date(time.dateTime);
    time.textContent = date.toLocaleString();
  });
});
