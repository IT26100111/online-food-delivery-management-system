document.addEventListener("DOMContentLoaded", () => {

    /* ==================================================
       HERO SLIDESHOW
    ================================================== */

    const slides =
        document.querySelectorAll(".hero-slide");

    const dots =
        document.querySelectorAll(".slide-dot");

    const heroCopies =
        document.querySelectorAll(".hero-copy");

    let currentSlide = 0;


    function showSlide(index) {

        slides.forEach((slide, i) => {

            slide.classList.toggle(
                "active",
                i === index
            );

        });


        dots.forEach((dot, i) => {

            dot.classList.toggle(
                "active",
                i === index
            );

        });


        heroCopies.forEach((copy, i) => {

            copy.classList.toggle(
                "active",
                i === index
            );

        });

    }


    function nextSlide() {

        currentSlide =
            (currentSlide + 1)
            % slides.length;

        showSlide(currentSlide);

    }


    let slideInterval =
        setInterval(
            nextSlide,
            4500
        );


    dots.forEach((dot, index) => {

        dot.addEventListener(
            "click",
            () => {

                currentSlide = index;

                showSlide(currentSlide);


                clearInterval(
                    slideInterval
                );


                slideInterval =
                    setInterval(
                        nextSlide,
                        4500
                    );

            }
        );

    });



    /* ==================================================
       SCROLL REVEAL
    ================================================== */

    const revealElements =
        document.querySelectorAll(
            ".reveal"
        );


    const revealObserver =
        new IntersectionObserver(

            entries => {

                entries.forEach(entry => {

                    if (
                        entry.isIntersecting
                    ) {

                        entry.target
                            .classList
                            .add("visible");


                        revealObserver
                            .unobserve(
                                entry.target
                            );

                    }

                });

            },

            {
                threshold: 0.12
            }

        );


    revealElements.forEach(
        element => {

            revealObserver.observe(
                element
            );

        }
    );



    /* ==================================================
       COUNT UP NUMBERS
    ================================================== */

    const counters =
        document.querySelectorAll(
            ".counter"
        );


    const counterObserver =
        new IntersectionObserver(

            entries => {

                entries.forEach(entry => {

                    if (
                        !entry.isIntersecting
                    ) {

                        return;

                    }


                    const counter =
                        entry.target;


                    const target =
                        Number(
                            counter.dataset.target
                        );


                    const duration =
                        1200;


                    const startTime =
                        performance.now();


                    function updateCounter(
                        currentTime
                    ) {

                        const progress =
                            Math.min(
                                (
                                    currentTime
                                    - startTime
                                )
                                / duration,
                                1
                            );


                        const value =
                            Math.floor(
                                progress
                                * target
                            );


                        counter.textContent =
                            value;


                        if (
                            progress < 1
                        ) {

                            requestAnimationFrame(
                                updateCounter
                            );

                        }

                        else {

                            counter.textContent =
                                target;

                        }

                    }


                    requestAnimationFrame(
                        updateCounter
                    );


                    counterObserver
                        .unobserve(
                            counter
                        );

                });

            },

            {
                threshold: 0.7
            }

        );


    counters.forEach(
        counter => {

            counterObserver.observe(
                counter
            );

        }
    );



    /* ==================================================
       RESTAURANT FILTER UI
    ================================================== */

    const filterButtons =
        document.querySelectorAll(
            ".filter-btn"
        );


    filterButtons.forEach(button => {

        button.addEventListener(
            "click",
            () => {

                filterButtons
                    .forEach(btn => {

                        btn.classList
                            .remove(
                                "active"
                            );

                    });


                button.classList
                    .add(
                        "active"
                    );

            }
        );

    });

});