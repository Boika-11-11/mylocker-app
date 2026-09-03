document.querySelectorAll('.pw-toggle').forEach(function (btn) {
    btn.addEventListener('click', function () {

        var input = document.getElementById(btn.getAttribute('data-target'));
        if (!input) {
            return;
        }

        var showing = input.type === 'text';

        input.type = showing ? 'password' : 'text';
        btn.classList.toggle('showing', !showing);
        btn.setAttribute('aria-pressed', String(!showing));
        btn.setAttribute('aria-label', showing ? 'Show password' : 'Hide password');

        input.focus();
    });
});
