import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Menu, X, Link, Users, Mail, GalleryHorizontalEnd, MessageCircle, Newspaper } from "lucide-react";

export function LinkUpLanding() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const scrollToSection = (sectionId: string) => {
    document.getElementById(sectionId)?.scrollIntoView({ behavior: "smooth" });
    setIsMenuOpen(false);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-primary/5 relative overflow-hidden">
      {/* Background decorative elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-primary/10 rounded-full blur-3xl"></div>
        <div className="absolute top-1/2 -left-40 w-60 h-60 bg-primary/5 rounded-full blur-3xl"></div>
        <div className="absolute bottom-20 right-1/4 w-40 h-40 bg-primary/8 rounded-full blur-2xl"></div>
      </div>
      {/* Header */}
      <header className="border-b bg-background/80 backdrop-blur-md sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-primary rounded-md flex items-center justify-center">
              <Link className="w-5 h-5 text-primary-foreground" />
            </div>
            <h1 className="text-2xl font-bold text-foreground">LinkUp!</h1>
          </div>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center space-x-8">
            <button
              onClick={() => scrollToSection("hero")}
              className="text-foreground hover:text-primary transition-colors"
            >
              Home
            </button>
            <button
              onClick={() => scrollToSection("about")}
              className="text-foreground hover:text-primary transition-colors"
            >
              About
            </button>
            <button
              onClick={() => scrollToSection("contact")}
              className="text-foreground hover:text-primary transition-colors"
            >
              Contact
            </button>
          </nav>

          <div className="flex items-center space-x-4">
            <Button asChild className="rounded-md hidden sm:block">
              <a href="/login">Login</a>
            </Button>

            {/* Mobile menu button */}
            <button
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="md:hidden p-2 text-foreground"
            >
              {isMenuOpen ? (
                <X className="w-5 h-5" />
              ) : (
                <Menu className="w-5 h-5" />
              )}
            </button>
          </div>
        </div>

        {/* Mobile Navigation */}
        {isMenuOpen && (
          <div className="md:hidden border-t bg-background/95 backdrop-blur-md">
            <nav className="container mx-auto px-4 py-4 space-y-4">
              <button
                onClick={() => scrollToSection("hero")}
                className="block text-foreground hover:text-primary transition-colors"
              >
                Home
              </button>
              <button
                onClick={() => scrollToSection("about")}
                className="block text-foreground hover:text-primary transition-colors"
              >
                About
              </button>
              <button
                onClick={() => scrollToSection("contact")}
                className="block text-foreground hover:text-primary transition-colors"
              >
                Contact
              </button>
              <Button asChild className="rounded-md w-full">
                <a href="/login">Login</a>
              </Button>
            </nav>
          </div>
        )}
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-12 relative">
        {/* Hero Section */}
        <section id="hero" className="text-center mb-16 relative">
          <div className="relative z-10">
            <div className="inline-flex items-center space-x-2 bg-primary/10 px-4 py-2 rounded-full mb-6 text-sm font-medium text-primary border border-primary/20">
              <Users className="w-4 h-4" />
              <span>Connect with purpose</span>
            </div>
            <h2 className="text-4xl md:text-6xl font-bold text-foreground mb-4 leading-tight">
              Welcome to <span className="text-primary">LinkUp!</span>
            </h2>
            <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto leading-relaxed">
              Connect, collaborate, and build meaningful relationships in a
              space designed for authentic connections
            </p>
            <div className="flex justify-center">
              <Button asChild size="lg" className="rounded-md px-8">
                <a href="/login">Get Started</a>
              </Button>
            </div>
          </div>
        </section>

        {/* About LinkUp Section */}
        <section id="about" className="mb-16">
          <Card className="rounded-md border-0 bg-card/50 backdrop-blur-sm shadow-lg">
            <CardHeader className="text-center">
              <div className="w-16 h-16 bg-primary rounded-md flex items-center justify-center mx-auto mb-4">
                <Link className="w-8 h-8 text-primary-foreground" />
              </div>
              <CardTitle className="text-3xl">About LinkUp</CardTitle>
              <CardDescription className="text-lg">
                Discover what makes our platform special
              </CardDescription>
            </CardHeader>
            <CardContent className="prose prose-slate max-w-none">
              <p className="text-muted-foreground text-center mb-8">
                Lorem ipsum dolor sit amet consectetur adipiscing elit. Quisque
                faucibus ex sapien vitae pellentesque sem placerat. In id cursus
                mi pretium tellus duis convallis. Tempus leo eu aenean sed diam
                urna tempor. Pulvinar vivamus fringilla lacus nec metus bibendum
                egestas. Iaculis massa nisl malesuada lacinia integer nunc
                posuere. Ut hendrerit semper vel class aptent taciti sociosqu.
                Ad litora torquent per conubia nostra inceptos himenaeos. Lorem
                ipsum dolor sit amet consectetur adipiscing elit. Quisque
                faucibus ex sapien vitae pellentesque sem placerat. In id cursus
                mi pretium tellus duis convallis. Tempus leo eu aenean sed diam
                urna tempor. Pulvinar vivamus fringilla lacus nec metus bibendum
                egestas. Iaculis massa nisl malesuada lacinia integer nunc
                posuere. Ut hendrerit semper vel class aptent taciti sociosqu.
                Ad litora torquent per conubia nostra inceptos himenaeos.{" "}
              </p>
              <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="text-center group hover:scale-105 transition-transform duration-200">
                  <div className="w-16 h-16 bg-gradient-to-br from-primary to-primary/80 rounded-md flex items-center justify-center mx-auto mb-4 group-hover:shadow-lg transition-shadow">
                    <span className="text-primary-foreground font-bold text-xl">
                      <GalleryHorizontalEnd />
                    </span>
                  </div>
                  <h3 className="font-semibold mb-2 text-lg">Stories</h3>
                  <p className="text-sm text-muted-foreground">
                    Post your own stories
                  </p>
                </div>
                <div className="text-center group hover:scale-105 transition-transform duration-200">
                  <div className="w-16 h-16 bg-gradient-to-br from-primary to-primary/80 rounded-md flex items-center justify-center mx-auto mb-4 group-hover:shadow-lg transition-shadow">
                    <span className="text-primary-foreground font-bold text-xl">
                      <MessageCircle />
                    </span>
                  </div>
                  <h3 className="font-semibold mb-2 text-lg">Live Chatting</h3>
                  <p className="text-sm text-muted-foreground">
                    Connect with your friends anytime
                  </p>
                </div>
                <div className="text-center group hover:scale-105 transition-transform duration-200">
                  <div className="w-16 h-16 bg-gradient-to-br from-primary to-primary/80 rounded-md flex items-center justify-center mx-auto mb-4 group-hover:shadow-lg transition-shadow">
                    <span className="text-primary-foreground font-bold text-xl">
                      <Newspaper />
                    </span>
                  </div>
                  <h3 className="font-semibold mb-2 text-lg">Personalized Feed</h3>
                  <p className="text-sm text-muted-foreground">
                    Discover something new about your friends
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </section>

        {/* Contact Section */}
        <section id="contact">
          <Card className="rounded-md border-0 bg-card/50 backdrop-blur-sm shadow-lg">
            <CardHeader className="text-center">
              <div className="w-16 h-16 bg-primary rounded-md flex items-center justify-center mx-auto mb-4">
                <Mail className="w-8 h-8 text-primary-foreground" />
              </div>
              <CardTitle className="text-3xl">Contact Us</CardTitle>
              <CardDescription className="text-lg">
                Get in touch with the LinkUp team
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="w-[40%] mx-auto">
                <h3 className="font-semibold mb-6 text-center text-xl">
                  Get in Touch
                </h3>
                <div className="space-y-4">
                  <div className="text-center p-4 bg-background/50 rounded-md border">
                    <p className="font-medium text-primary">Email</p>
                    <p className="text-muted-foreground">contact@linkup.com</p>
                  </div>
                  <div className="text-center p-4 bg-background/50 rounded-md border">
                    <p className="font-medium text-primary">Support</p>
                    <p className="text-muted-foreground">support@linkup.com</p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </section>
      </main>

      {/* Footer */}
      <footer className="border-t mt-16">
        <div className="container mx-auto px-4 py-6 text-center">
          <p className="text-muted-foreground">
            © 2025 LinkUp! All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}
