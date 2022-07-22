<template id = "members">
<div>
  <app-header></app-header>
  <br>

  <div class="members ui content container">
    <h2 class="ui center aligned icon header">
      <i class="fa-solid fa-users"></i>
      <p>The Inquisition's members:</p>
    </h2>
    <div class="members-contain ui link cards" style="justify-content: center;">
      <div class="submember ui card" style="justify-content: center;" v-for="member in members" :key="member.name">
        <a :href =member.url>
        <img :src="member.avatar" width="200" height="200" class="centre"/>
        <h2 style="text-align: center; padding: 5px; font-weight: bolder; font-family: 'Comic Sans MS', 'Comic Sans', cursive">{{member.name}}</h2>
        </a>
      </div>
    </div>
  </div>

  <app-footer />
</div>
</template>
<head>
  <script src="/dist/jquery-3.6.0.min.js"></script>
  <title>Inquisition's members</title>
  <meta name="color-scheme" content="light dark">
  <link rel="icon" href="/favicon.png" type="image/x-icon">
  <meta name="description" content="The great people behind the Inquisition!">
  <meta content="technology, programming, community, Minecraft, modding" name="keywords">
</head>
<script>
  doLoad()

  async function doLoad() {
    const session = sessionStorage.getItem('members')
    if (session) finishLoading(JSON.parse(session))
    else {
      const members = await getHTML("https://api.github.com/orgs/TheModdingInquisition/members?page=1&per_page=100")
      sessionStorage.setItem('members', members)
      window.location.reload()
    }
  }

  function getHTML(url) {
    return new Promise(function (resolve, reject) {
      var xhr = new XMLHttpRequest();
      xhr.open('get', url, true);
      xhr.onload = function () {
        var status = xhr.status;
        if (status == 200) {
          resolve(xhr.responseText);
        } else {
          reject(status);
        }
      };
      xhr.send();
    });
  }

  function finishLoading(members) {
    const actualMembers = members.map(e => ({
      name: trim(e.login, 20),
      url: e.html_url,
      avatar: e.avatar_url
    }));
    Vue.component("members", {
      template: "#members",
      data: () => ({
        members: actualMembers
      }),
    });
  }

  function trim(string, len) {
    if (string.length > len) {
      return string.substring(0, len - 3) + "..."
    }
    return string
  }

  window.onload = () => updateHeader('members')
</script>
<style>
.centre {
  display: block;
  margin-left: auto;
  margin-right: auto;
  padding: 5px;
}
</style>