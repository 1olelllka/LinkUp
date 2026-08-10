import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Menu,
  X,
  Link as LinkIcon,
  Pin,
  MessageCircle,
  Rss,
  Image as ImageIcon,
  Mail,
} from "lucide-react";
import { ensureAccessToken } from "@/utils/ensureAccessToken";

const BOARD_W = 640;
const BOARD_H = 460;

const nodes = [
  { id: "you", label: "you", x: 320, y: 248 },
  { id: "roommates", label: "roommates", x: 118, y: 96 },
  { id: "study", label: "study group", x: 508, y: 82 },
  { id: "hometown", label: "hometown crew", x: 86, y: 344 },
  { id: "team", label: "teammates", x: 546, y: 330 },
];

const strings = [
  "M320,248 Q182,142 118,96",
  "M320,248 Q462,128 508,82",
  "M320,248 Q150,332 86,344",
  "M320,248 Q482,320 546,330",
  "M320,248 Q300,352 322,428",
];

const features = [
  {
    icon: ImageIcon,
    title: "Stories",
    body: "Post a photo, a rant, a win. Up for a day, seen by your circle — no filters required.",
    rotate: "-rotate-2",
  },
  {
    icon: MessageCircle,
    title: "Live chatting",
    body: "Jump straight into a conversation instead of waiting on a reply. Threads for the people you actually talk to.",
    rotate: "rotate-1",
  },
  {
    icon: Rss,
    title: "Personalized feed",
    body: "See what your friends are up to, sorted by who you care about — not by what keeps you scrolling.",
    rotate: "-rotate-1",
  },
];

export function LinkUpLanding() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(true);

  useEffect(() => {
      let mounted = true;
      (async () => {
      try {
          const token = await ensureAccessToken();
          if (mounted) {
          setIsAuthenticated(!!token);
          }
      } catch {
          if (mounted) {
          setIsAuthenticated(false);
          }
      }
      })();
      return () => {
      mounted = false;
      };
  })

  const scrollToSection = (sectionId) => {
    document.getElementById(sectionId)?.scrollIntoView({ behavior: "smooth" });
    setIsMenuOpen(false);
  };

  return (
    <div
      className="min-h-screen bg-[#1E1A16] text-[#F3EBD9]"
      style={{ fontFamily: "'IBM Plex Sans', sans-serif" }}
    >
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Space+Mono:wght@400;700&family=IBM+Plex+Sans:wght@400;500;600;700&family=Caveat:wght@500;600;700&display=swap');
        .font-display { font-family: 'Space Mono', monospace; }
        .font-hand { font-family: 'Caveat', cursive; }
        .string-path {
          stroke-dasharray: 700;
          stroke-dashoffset: 700;
          animation: draw-string 1.4s ease-out forwards;
        }
        .string-path:nth-child(1) { animation-delay: 0.1s; }
        .string-path:nth-child(2) { animation-delay: 0.3s; }
        .string-path:nth-child(3) { animation-delay: 0.5s; }
        .string-path:nth-child(4) { animation-delay: 0.7s; }
        .string-path:nth-child(5) { animation-delay: 0.9s; }
        @keyframes draw-string { to { stroke-dashoffset: 0; } }
        @media (prefers-reduced-motion: reduce) {
          .string-path { animation: none; stroke-dashoffset: 0; }
        }
      `}</style>

      {/* Header */}
      <header className="border-b border-[#3A322A] bg-[#1E1A16]/90 backdrop-blur-md sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-[#C9A063] rounded-sm flex items-center justify-center rotate-3">
              <LinkIcon className="w-4 h-4 text-[#1E1A16]" />
            </div>
            <h1 className="text-2xl font-display font-bold tracking-tight">LinkUp!</h1>
          </div>

          <nav className="hidden md:flex items-center space-x-8 text-sm">
            <button
              onClick={() => scrollToSection("hero")}
              className="hover:text-[#D9A441] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#D9A441] rounded-sm"
            >
              Home
            </button>
            <button
              onClick={() => scrollToSection("about")}
              className="hover:text-[#D9A441] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#D9A441] rounded-sm"
            >
              About
            </button>
            <button
              onClick={() => scrollToSection("contact")}
              className="hover:text-[#D9A441] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#D9A441] rounded-sm"
            >
              Contact
            </button>
          </nav>

          <div className="flex items-center space-x-4">
            {isAuthenticated
            ?
            <Button
              asChild
              className="hidden sm:inline-flex rounded-sm bg-[#B23A2E] hover:bg-[#9c3226] text-[#F3EBD9] font-medium"
            >
              <a href="/profile">My Profile</a>
            </Button>
            :
            <Button
              asChild
              className="hidden sm:inline-flex rounded-sm bg-[#B23A2E] hover:bg-[#9c3226] text-[#F3EBD9] font-medium"
            >
              <a href="/login">Log in</a>
            </Button>
            }
            <button
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="md:hidden p-2"
              aria-label="Toggle menu"
            >
              {isMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {isMenuOpen && (
          <div className="md:hidden border-t border-[#3A322A] bg-[#1E1A16]">
            <nav className="container mx-auto px-4 py-4 flex flex-col space-y-4 text-sm">
              <button onClick={() => scrollToSection("hero")} className="text-left hover:text-[#D9A441]">
                Home
              </button>
              <button onClick={() => scrollToSection("about")} className="text-left hover:text-[#D9A441]">
                About
              </button>
              <button onClick={() => scrollToSection("contact")} className="text-left hover:text-[#D9A441]">
                Contact
              </button>
              <Button asChild className="rounded-sm bg-[#B23A2E] hover:bg-[#9c3226] w-full">
                <a href="/login">Log in</a>
              </Button>
            </nav>
          </div>
        )}
      </header>

      <main>
        {/* Hero */}
        <section id="hero" className="container mx-auto px-4 pt-14 pb-20">
          <div className="text-center max-w-2xl mx-auto mb-12">
            <span className="font-hand text-2xl text-[#D9A441] block mb-1">
              no cold feed, no strangers
            </span>
            <h2 className="font-display text-4xl md:text-6xl font-bold leading-tight mb-4">
              Pin the people who matter.
            </h2>
            <p className="text-[#CBBFA0] text-lg leading-relaxed mb-8 max-w-xl mx-auto">
              LinkUp turns your circle into a living board — stories, chats, and updates
              from people you actually know, all mapped in one place.
            </p>
            <Button
              asChild
              size="lg"
              className="rounded-sm bg-[#B23A2E] hover:bg-[#9c3226] text-[#F3EBD9] px-8 font-medium"
            >
            {isAuthenticated  
            ?
              <a href="/profile">Get started</a>
            :
              <a href="/login">Get started</a>
            }
            </Button>
          </div>

          {/* Corkboard */}
          <div className="relative mx-auto max-w-[640px] p-3 sm:p-5 bg-[#6B4A32] rounded-md shadow-2xl">
            <div
              className="relative w-full rounded-sm overflow-hidden"
              style={{
                aspectRatio: `${BOARD_W} / ${BOARD_H}`,
                backgroundColor: "#E8DFC8",
                backgroundImage: "radial-gradient(rgba(107,74,50,0.14) 1px, transparent 1px)",
                backgroundSize: "14px 14px",
              }}
            >
              <svg
                viewBox={`0 0 ${BOARD_W} ${BOARD_H}`}
                className="absolute inset-0 w-full h-full"
                preserveAspectRatio="none"
              >
                {strings.map((d, i) => (
                  <path
                    key={i}
                    d={d}
                    fill="none"
                    stroke="#B23A2E"
                    strokeWidth="2.5"
                    strokeLinecap="round"
                    className="string-path"
                  />
                ))}
              </svg>

              {nodes.map((n) => (
                <div
                  key={n.id}
                  className="absolute flex flex-col items-center"
                  style={{
                    left: `${(n.x / BOARD_W) * 100}%`,
                    top: `${(n.y / BOARD_H) * 100}%`,
                    transform: "translate(-50%, -50%)",
                  }}
                >
                  <Pin
                    className="w-4 h-4 -mb-1 z-10 drop-shadow-sm rotate-[-20deg]"
                    style={{ color: "#D9A441" }}
                    fill="#D9A441"
                  />
                  <div
                    className={`${
                      n.id === "you" ? "w-16 h-16 text-base" : "w-12 h-12 text-sm"
                    } rounded-full bg-[#1E1A16] text-[#F3EBD9] flex items-center justify-center font-display font-bold shadow-md border-2 border-[#C9A063]`}
                  >
                    {n.id === "you" ? "you" : n.label.charAt(0).toUpperCase()}
                  </div>
                  {n.id !== "you" && (
                    <span className="font-hand text-lg text-[#241F1A] mt-1 whitespace-nowrap">
                      {n.label}
                    </span>
                  )}
                </div>
              ))}

              {/* group chat note */}
              <div
                className="absolute bg-[#F3EBD9] border border-[#C9A063] rounded-sm px-3 py-2 shadow-md flex items-center gap-1.5"
                style={{
                  left: `${(320 / BOARD_W) * 100}%`,
                  top: `${(432 / BOARD_H) * 100}%`,
                  transform: "translate(-50%, -50%) rotate(-3deg)",
                }}
              >
                <Pin
                  className="w-3.5 h-3.5 absolute -top-2 left-1/2 -translate-x-1/2 rotate-[-15deg]"
                  style={{ color: "#D9A441" }}
                  fill="#D9A441"
                />
                <MessageCircle className="w-3.5 h-3.5 text-[#B23A2E]" />
                <span className="font-hand text-base text-[#241F1A]">the group chat</span>
              </div>
            </div>
          </div>
        </section>

        {/* About */}
        <section id="about" className="container mx-auto px-4 py-16">
          <div className="text-center max-w-2xl mx-auto mb-12">
            <span className="font-hand text-xl text-[#D9A441]">about</span>
            <h3 className="font-display text-3xl md:text-4xl font-bold mt-1 mb-4">
              Built for the people you already know.
            </h3>
            <p className="text-[#CBBFA0] leading-relaxed">
              Most networks optimize for strangers. LinkUp does the opposite — a small,
              personal board for your circle: old classmates, teammates, the group chat
              that never sleeps. No algorithm chasing, no ads. Just your people.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl mx-auto pt-4">
            {features.map((f) => (
              <div
                key={f.title}
                className={`relative bg-[#E8DFC8] text-[#241F1A] rounded-sm p-6 pt-8 shadow-lg ${f.rotate} hover:rotate-0 hover:-translate-y-1 transition-transform duration-300`}
              >
                <Pin
                  className="w-5 h-5 absolute -top-2.5 left-1/2 -translate-x-1/2 rotate-[-10deg] drop-shadow"
                  style={{ color: "#D9A441" }}
                  fill="#D9A441"
                />
                <f.icon className="w-7 h-7 text-[#B23A2E] mb-3" />
                <h4 className="font-display font-bold text-lg mb-2">{f.title}</h4>
                <p className="text-sm text-[#4A4136] leading-relaxed">{f.body}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Contact */}
        <section id="contact" className="container mx-auto px-4 py-16">
          <div className="text-center max-w-xl mx-auto mb-10">
            <span className="font-hand text-xl text-[#D9A441]">leave a note</span>
            <h3 className="font-display text-3xl md:text-4xl font-bold mt-1">Get in touch</h3>
          </div>

          <div className="flex flex-col sm:flex-row gap-6 justify-center max-w-xl mx-auto">
            <div className="relative flex-1 bg-[#E8DFC8] text-[#241F1A] rounded-sm p-6 pt-8 shadow-lg rotate-1 hover:rotate-0 transition-transform duration-300">
              <Pin
                className="w-5 h-5 absolute -top-2.5 left-1/2 -translate-x-1/2 rotate-[8deg]"
                style={{ color: "#D9A441" }}
                fill="#D9A441"
              />
              <Mail className="w-5 h-5 text-[#B23A2E] mb-2" />
              <p className="font-display font-bold mb-1">Email</p>
              <p className="text-sm text-[#4A4136]">contact@linkup.com</p>
            </div>
            <div className="relative flex-1 bg-[#E8DFC8] text-[#241F1A] rounded-sm p-6 pt-8 shadow-lg -rotate-1 hover:rotate-0 transition-transform duration-300">
              <Pin
                className="w-5 h-5 absolute -top-2.5 left-1/2 -translate-x-1/2 rotate-[-8deg]"
                style={{ color: "#D9A441" }}
                fill="#D9A441"
              />
              <Mail className="w-5 h-5 text-[#B23A2E] mb-2" />
              <p className="font-display font-bold mb-1">Support</p>
              <p className="text-sm text-[#4A4136]">support@linkup.com</p>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="border-t border-[#3A322A] mt-8">
        <div className="container mx-auto px-4 py-6 text-center">
          <p className="text-sm text-[#8A7F6C] font-display">© 2026 LinkUp!</p>
        </div>
      </footer>
    </div>
  );
}

export default LinkUpLanding;