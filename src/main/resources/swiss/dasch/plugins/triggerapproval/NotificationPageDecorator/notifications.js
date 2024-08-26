function installNotificationService(factory, notifications) {
  function startPolling() {
    factory.create(function(factoryResponse) {
      const service = eval(factoryResponse.responseObject());
      
      function handleResponse(response) {
        const obj = response.responseObject();
        
        if (obj.status != "done" && obj.status != "canceled" && obj.status != "error") {
          setTimeout(fetch, 500);
        } else if(obj.status == "done" && obj.data.notification && obj.data.notification in notifications) {
          let notification = notifications[obj.data.notification];
          window.notificationBar.show(notification.message, notification.settings);
          clearPollOnRefresh();
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
  
  const pollOnRefreshStorageKey = "swiss.dasch.plugins.triggerapproval.NotificationPageDecorator.notifications.pollOnRefresh";
  
  function shouldPollOnRefresh() {
    return window.sessionStorage.getItem(pollOnRefreshStorageKey);
  }
  
  function setPollOnRefresh() {
    window.sessionStorage.setItem(pollOnRefreshStorageKey, true);
  }
  
  function clearPollOnRefresh() {
    window.sessionStorage.removeItem(pollOnRefreshStorageKey);
  }
  
  if(shouldPollOnRefresh()) {
    document.addEventListener("DOMContentLoaded", function() {
      clearPollOnRefresh();
      startPolling();
    });
  }
  
  const buildUrlRegex = new RegExp("(^|/)job/[^/]+/build(\\?[^/]+)?$");
  const open = window.XMLHttpRequest.prototype.open;
  window.XMLHttpRequest.prototype.open = function(method, url, ...args) {
    if(buildUrlRegex.test(url)) {
      setPollOnRefresh();
      startPolling();
    }
    return open.call(this, method, url, ...args);
  };
}