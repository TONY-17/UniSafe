;
; Simple I/O Routines - Version 2
;

IFNDEF IO_INC
IO_INC equ <1>

OutputStr PROTO NEAR32 STDCALL :DWORD
OutputInt PROTO NEAR32 STDCALL :DWORD
OutputFloat PROTO NEAR32 STDCALL :DWORD
OutputFloat8 PROTO NEAR32 STDCALL :QWORD
InputInt PROTO NEAR32 STDCALL
InputStr PROTO NEAR32 STDCALL :DWORD, :DWORD

ELSE
echo --------------------------------------
echo WARNING Duplicate include file io.inc
echo --------------------------------------
ENDIF
