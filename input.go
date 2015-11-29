package main

x int;
x = 10;
y float;
y = 15.5;
z float;
z = 99.9;

// Need to work on pass by reference
func passByReference(a *int, b float, c *float) void {
    Println("Inside function");
    Println(a);
    Println(b);
    Println(c);
    a = 5;
    b = 25.5;
    c = 100.123;
    Println(a);
    Println(b);
    Println(c);
    Println(x); // This would be 5 in Java. However, due to the limitations of the teacher's implementation
                // It x doesn't become 5 until it leaves the function and unwraps the Wrap class
    Println("Outside function");
}


passByReference(x, y, z);
Println(x);
Println(y);
Println(z);
