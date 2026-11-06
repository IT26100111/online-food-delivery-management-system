document.addEventListener(
    "DOMContentLoaded",
    () => {

        const filterButtons =
            document.querySelectorAll(
                ".public-filter"
            );


        const restaurantItems =
            document.querySelectorAll(
                ".restaurant-item"
            );


        const searchInput =
            document.getElementById(
                "restaurantSearch"
            );


        const emptyState =
            document.getElementById(
                "restaurantEmpty"
            );


        let activeFilter =
            "all";



        /* =============================================
           FILTER RESTAURANTS
        ============================================= */


function filterRestaurants() {

    const searchValue = searchInput.value
        .trim()
        .toLowerCase();

    let visibleCount = 0;

    restaurantItems.forEach(item => {

        const category = (
            item.dataset.category || ""
        ).trim().toLowerCase();

        const searchText = (
            item.dataset.name || ""
        ).trim().toLowerCase();

        const matchesFilter =
            activeFilter === "all" ||
            category === activeFilter.toLowerCase();

        const matchesSearch =
            searchText.includes(searchValue);

        const shouldShow =
            matchesFilter && matchesSearch;

        item.style.display = shouldShow ? "" : "none";

        if (shouldShow) {
            visibleCount++;
        }
    });

    emptyState.style.display =
        visibleCount === 0 ? "block" : "none";
}




        /* =============================================
           CATEGORY BUTTONS
        ============================================= */

        filterButtons.forEach(
            button => {

                button.addEventListener(
                    "click",
                    () => {

                        filterButtons
                            .forEach(
                                btn =>
                                    btn.classList
                                        .remove(
                                            "active"
                                        )
                            );


                        button.classList
                            .add(
                                "active"
                            );


                        activeFilter =
                            button.dataset.filter;


                        filterRestaurants();

                    }
                );

            }
        );



        /* =============================================
           SEARCH
        ============================================= */

        searchInput.addEventListener(
            "input",
            filterRestaurants
        );



        /* =============================================
           FAVOURITES
        ============================================= */

        const favouriteButtons =
            document.querySelectorAll(
                ".restaurant-favourite"
            );


        favouriteButtons.forEach(
            button => {

                button.addEventListener(
                    "click",
                    () => {

                        button.classList
                            .toggle(
                                "active"
                            );


                        const icon =
                            button.querySelector(
                                "i"
                            );


                        icon.classList.toggle(
                            "fa-regular"
                        );


                        icon.classList.toggle(
                            "fa-solid"
                        );

                    }
                );

            }
        );

    }
);