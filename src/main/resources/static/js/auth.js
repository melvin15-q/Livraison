document.addEventListener("DOMContentLoaded", function () {
  // Afficher/masquer un mot de passe (fonctionne pour tout champ .toggle-eye)
  document.querySelectorAll(".toggle-eye").forEach(function (btn) {
    btn.addEventListener("click", function () {
      var input = document.getElementById(btn.dataset.target);
      if (!input) return;
      input.type = input.type === "password" ? "text" : "password";
    });
  });

  // Vérification en direct que les deux mots de passe correspondent (inscription)
  var password = document.getElementById("password");
  var confirmPassword = document.getElementById("confirmPassword");
  var confirmHint = document.getElementById("confirmPasswordHint");

  if (password && confirmPassword && confirmHint) {
    function checkMatch() {
      if (confirmPassword.value.length === 0) {
        confirmHint.textContent = "";
        confirmHint.classList.remove("hint-error");
        confirmPassword.closest(".input-wrap").classList.remove("has-error");
        return;
      }
      if (password.value !== confirmPassword.value) {
        confirmHint.textContent = "Les mots de passe ne correspondent pas.";
        confirmHint.classList.add("hint-error");
        confirmPassword.closest(".input-wrap").classList.add("has-error");
      } else {
        confirmHint.textContent = "Les mots de passe correspondent.";
        confirmHint.classList.remove("hint-error");
        confirmPassword.closest(".input-wrap").classList.remove("has-error");
      }
    }
    password.addEventListener("input", checkMatch);
    confirmPassword.addEventListener("input", checkMatch);
  }
});
