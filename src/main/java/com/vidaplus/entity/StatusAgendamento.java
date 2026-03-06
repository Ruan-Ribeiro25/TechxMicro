package com.vidaplus.entity;

public enum StatusAgendamento {
    AGUARDANDO,       // Paciente na recepção
    CONFIRMADO,       // Confirmado no sistema
    EM_ATENDIMENTO,   // Com o médico
    CONCLUIDO,        // Finalizado
    CANCELADO,        // Cancelado
    FALTOU,           // Não apareceu
    
    // --- LEGADO (Necessário para ler dados antigos do banco) ---
    Agendado,
    Agendada,
    AGENDADO
}