-module (Main).
int X.
X = 10.

func PassByReference(A *int) void
{
	io:fwrite(A).
	A = 5.
	
	io:fwrite(A).
	
	io:fwrite(X).
}.

PassByReference(X).
io:fwrite(X).