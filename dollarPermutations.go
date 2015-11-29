package main
quarter int;
dime int;
nickel int;
penny int;

for quarter = 0; quarter <= 4; quarter++ {
    for dime = 0; dime <= 10; dime++ {
        for nickel = 0; nickel <= 20; nickel++ {
            for penny = 0; penny <= 100; penny++ {
                if quarter * 25 + dime * 10 + nickel * 5 + penny == 100 {
                    Println("These make up a dollar");
                    Println("Quarters: " + quarter);
                    Println("Dimes: " + dime);
                    Println("Nickels: " + nickel);
                    Println("Pennies: " + penny);
                }
            }
        }
    }
}

