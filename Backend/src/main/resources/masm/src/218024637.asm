;	Update all of this information to reflect your own details and the prac
;	Author:     Dr J du Toit
;	Template document
.386
.MODEL FLAT ; Flat memory model
.STACK 4096 ; 4096 bytes

; Exit function
ExitProcess PROTO NEAR32 stdcall, dwExitCode:DWORD

; The data section stores all global variables
.DATA

; The code section may contain multiple tags such as _start, which is the entry
; point of this assembly program
.CODE
_start:
	; Most of your initial code will be under the _start tag.
	; The _start tag is just a memory address referenced by the Public indicator
	; highlighting which functions are available to calling functions.
	; _start gets called by the operating system to start this process.
 

	; We call the Operating System ExitProcess system call to close the process.
	INVOKE ExitProcess, 0
Public _start
END
