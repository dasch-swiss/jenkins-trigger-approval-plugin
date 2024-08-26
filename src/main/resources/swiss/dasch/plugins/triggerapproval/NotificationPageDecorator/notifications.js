function installNotificationService(factory, notifications) {
  function startPolling() {
    factory.create(function(factoryResponse) {
      let service = eval(factoryResponse.responseObject());
      
      function handleResponse(response) {
        let obj = response.responseObject();
        
        if (obj.status != "done" && obj.status != "canceled" && obj.status != "error") {
          setTimeout(fetch, 500);
        } else if(obj.status == "done" && obj.data.notification && obj.data.notification in notifications) {
          let notification = notifications[obj.data.notification];
          window.notificationBar.show(notification.message, notification.settings);
        }
      }
      
      function fetch() {
        service.news(handleResponse);
      }
      
      service.start(function() {
        fetch();
      });
    });
  }
  
  document.addEventListener("DOMContentLoaded", function() {
    document.querySelectorAll(".task-link, .jenkins-table__button").forEach(function(element) {
      element.addEventListener("click", startPolling, true);
    });
  });
}