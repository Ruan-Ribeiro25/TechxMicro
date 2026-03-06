// Arquivo: src/main/resources/static/js/script.js

// Exemplo de função para mostrar alertas
function showAlert(message) {
    const alertBox = document.createElement('div');
    alertBox.innerText = message;
    alertBox.style.backgroundColor = '#28a745';
    alertBox.style.color = '#fff';
    alertBox.style.padding = '10px';
    alertBox.style.margin = '10px 0';
    alertBox.style.borderRadius = '5px';
    document.body.prepend(alertBox);

    setTimeout(() => {
        alertBox.remove();
    }, 3000);
}

// Validação simples de formulário
function validateForm(formId) {
    const form = document.getElementById(formId);
    let valid = true;
    form.querySelectorAll('input, textarea, select').forEach(field => {
        if (!field.value) {
            field.style.borderColor = 'red';
            valid = false;
        } else {
            field.style.borderColor = '#ccc';
        }
    });
    return valid;
}
