About: Repositories;
Some repositories will be located in the 
- financial_tracker folder
- common/repository folder
it depends...

Q1. What is the difference between common & financial_tracker folders?
-   common contains files that are reusable between different types of applications like Users/customers are reusable 
    between financial websites & e-commerce websites, whereas shopping_cart is specific to e-commerce and would therefore 
    fall under e_commerce folder.
-   Financial_tracker contains all the files specific to a finance or in this case specifically, a finance_tracker app.


Q2. You cannot have a "common" file for the controller category in SpringBoot, 
Controllers are only for the interface level. So depending on how many interfaces you have, there would be X amount of
controllers. Whereas services there can be many. 
-   controllers are only for interface levels and only if you have a mapping then you have an interface.
-   The only thing shared would be the service layer. There is only one service layer whereby the usage should be
    shared and used by both users & admin & master_Admins. 
-   Unless of course there is like complex business logics that are user specific, admin specific or master_admin specific.
-   The way the security works is via the controller, if they say @PreAuthorize and the second is not allowing any mappings
    for sensitive commands (linkage to service methods). 

List of future to-dos:
However, as mentioned previously, this approach for getCurrentUserId() is less efficient because it performs a database 
lookup on every request that calls it. The more optimized way is to store the user's ID directly in a custom UserDetails 
implementation (UserDetailsImpl) within the Spring Security context, avoiding repeated database calls. You can implement 
that optimization after you confirm the role-based access is working.
