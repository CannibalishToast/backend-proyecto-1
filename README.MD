# Backend Proyecto 1 – Secure Genomic Intake and Disease Detection Server

Este proyecto implementa un servidor y cliente en **Java** que se comunican mediante **sockets TCP sobre SSL/TLS** usando un certificado autofirmado.  
El sistema está diseñado para manejar pacientes, metadatos y archivos FASTA de enfermedades como parte de un flujo seguro de datos genómicos.

---

## 🚀 Requisitos previos

- **Java JDK 17** o superior
- **Apache Maven 3.9.11+**
- Git (opcional, para clonar el repositorio)

El proyecto utiliza un protocolo de texto customizado, documentado a continuación:

📌 Estructura general del mensaje
📤 Mensaje del cliente
COMMAND: <NOMBRE_COMANDO>
clave1=valor1
clave2=valor2
...
END


COMMAND: → acción a ejecutar.

clave=valor → parámetros de la operación.

END → marca el fin del mensaje.

📥 Respuesta del servidor

Respuesta exitosa

STATUS: OK
message=Paciente creado con ID P-abcd1234
END


Respuesta de error

STATUS: ERROR
code=422
message=Faltan campos obligatorios
END

📌 Comandos soportados
🔹 CREATE_PATIENT

Crea un nuevo paciente con metadata y opcionalmente un archivo FASTA.

Campos obligatorios

full_name, document_id, age, sex, email

Opcionales

clinical_notes

fasta_content (usar \n para saltos de línea)

Ejemplo:

COMMAND: CREATE_PATIENT
full_name=Juan Perez
document_id=12345
age=30
sex=M
email=juan@example.com
fasta_content=>seq1\nACGTACGT\n
END

🔹 GET_PATIENT

Consulta la información de un paciente.

Campos requeridos

patient_id

Ejemplo:

COMMAND: GET_PATIENT
patient_id=P-abcd1234
END


Respuesta:

STATUS: OK
patient_id=P-abcd1234
full_name=Juan Perez
document_id=12345
age=30
sex=M
contact_email=juan@example.com
registration_date=2025-09-10T01:00:00
clinical_notes=
checksum_fasta=e2fc714c4727ee9395f324cd2e7f331f
file_size_bytes=14
active=true
END

🔹 UPDATE_PATIENT

Actualiza datos de un paciente.

patient_id es obligatorio.

Los demás campos se actualizan solo si se envían.

Ejemplo:

COMMAND: UPDATE_PATIENT
patient_id=P-abcd1234
age=31
fasta_content=>seq1\nTTTTACGT\n
END

🔹 DELETE_PATIENT

Marca un paciente como inactivo.

Ejemplo:

COMMAND: DELETE_PATIENT
patient_id=P-abcd1234
END

🔹 PING

Chequeo de conectividad.

Ejemplo:

COMMAND: PING
END


Respuesta:

STATUS: OK
message=PONG
END

📌 Códigos de error
Código	Tipo de error	Ejemplo
400	Comando no reconocido	COMMAND: FOO_BAR
404	Recurso no encontrado	Paciente inexistente o ya inactivo
409	Conflicto	Paciente con document_id duplicado
422	Input inválido	Falta campo obligatorio / FASTA malformado
500	Error interno del servidor	Excepción inesperada
📌 Atributos especiales de pacientes

checksum_fasta → Hash MD5 del archivo FASTA (integridad).

file_size_bytes → Tamaño en bytes del archivo FASTA.

Ambos se calculan automáticamente en el servidor al recibir un archivo.

📌 Flujo de ejemplo
Crear paciente
COMMAND: CREATE_PATIENT
full_name=Maria Lopez
document_id=67890
age=25
sex=F
email=maria@example.com
fasta_content=>seq1\nTTTTACGTACGT\n
END


Respuesta:

STATUS: OK
message=Paciente creado con ID P-xyz98765
END
STATUS: OK
message=No se detectaron enfermedades conocidas.
END

Consultar paciente
COMMAND: GET_PATIENT
patient_id=P-xyz98765
END


Respuesta:

STATUS: OK
patient_id=P-xyz98765
full_name=Maria Lopez
document_id=67890
age=25
sex=F
contact_email=maria@example.com
registration_date=2025-09-10T01:10:00
clinical_notes=
checksum_fasta=098f6bcd4621d373cade4e832627b4f6
file_size_bytes=12
active=true
END