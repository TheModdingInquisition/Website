<template id="app-footer">
  <footer class="ui inverted vertical sinking segment container" id="footer" style="margin-top: 10px">
    <div class="ui center aligned container">
      <p style="text-align: center;"><a class="no-link-footer socials_link"
                                        href="https://url.moddinginquisition.org/discord">
        <img class="socials-icon" src="/dist/icons/discord.svg" alt="discord">
        <strong>Discord</strong></a>
        &bull; <a class="no-link-footer socials_link" href="https://github.com/TheModdingInquisition">
          <img class="socials-icon" src="/dist/icons/github.svg" alt="github">
          No one expects the @&ZeroWidthSpace;<strong>TheModdingInquisition</strong></a> &bull;
        <a class="no-link-footer socials_link"
           href="https://url.moddinginquisition.org/recruitment">
          <img class="socials-icon" src="/dist/icons/recruitment.png" alt="recruitment">
          <strong>Recruitment</strong></a>
      </p>
      <p style="text-align: center;">Copyright &copy; 2022 <a class="no-link-footer"
                                                              style="color: #ffffff"
                                                              href="https://github.com/TheModdingInquisition">Modding
        Inquisition</a></p>
    </div>
  </footer>
</template>
<style>
/* Social links, in the footer */
.socials_link {
  display: inline-block;
  border-radius: 1em;
  color: #eee;
  background-color: #1f1f1f;
  padding: 0.1em 0.6em;
  text-decoration: none;
}

.no-link-footer {
  color: #aaf;
  text-decoration: none;
  font-family: Lucida Sans,serif;
}

/* Inline icons match font size, and are aligned in the middle of text */
.socials-icon {
  width: 1em;
  vertical-align: middle;
}
</style>