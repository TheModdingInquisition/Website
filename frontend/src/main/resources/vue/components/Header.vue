<template id="app-header">
  <header>
    <div class="ui secondary menu">
      <div class="ui container">
        <a class="header item" href="/"> <img alt="logo" class="ui image" src="/favicon.png" id="logo" height="50px"></a>
        <a class="item" id = "header-home" href="/">Home</a>
        <a class="item" id = "header-members" href="/members">Members</a>
        <div class="right menu">
          <a class="item" id = "log-button"></a>
          <a class="item" href="/client_api/self_user"><img class="ui right item" src="/client_api/self_avatar" height="75px" /></a>
        </div>
      </div>
    </div>
  </header>
</template>
<script>
const xhr = new XMLHttpRequest()
xhr.open("GET", "/client_api/is_logged_in")
xhr.send()
xhr.onload = function() {
  const logButton = document.getElementById('log-button')
  if (xhr.responseText == 'true') {
    logButton.href = '/logout'
    logButton.innerText = 'Logout'
  } else {
    logButton.href = '/login'
    logButton.innerText = 'Login'
  }
}
</script>