PROGRAM lab1_ej7(INPUT,OUTPUT);
USES crt;
VAR 
	lado,area:INTEGER;
BEGIN
 clrscr;
 WRITE('Introduzca el lado del cuadrado (No. Entero positivo): ');
 READLN(lado);
 area:=sqr(lado);
 WRITE('El  rea del cuadrado con lado ',lado,' es: ',area);
 READKEY;
END.
