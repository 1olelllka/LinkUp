

export const SidebarLeft = () => {

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-3">
        <img
          src="/avatar.jpg"
          alt="User avatar"
          className="w-12 h-12 rounded-full"
        />
        <div>
          <h4 className="font-bold text-lg">My Username</h4>
        </div>
      </div>

      <nav className="space-y-2">
        {["Feed", "Messages", "My Profile", "Settings"].map((item, i) => (
          <button
            key={i}
            className={`block w-full text-left px-4 py-2 rounded-lg hover:bg-gray-500 ${
              i === 0 ? "bg-black text-white font-semibold" : ""
            }`}
          >
            {item}
          </button>
        ))}
      </nav>
    </div>
  );
}