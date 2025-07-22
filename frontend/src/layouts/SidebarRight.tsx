export const SidebarRight = () => {
  return (
    <div className="space-y-6">
      <section>
        <h4 className="text-lg font-semibold mb-2">Stories</h4>
        <div className="flex space-x-2 overflow-x-auto">
          <img src="/story_img.jpg" className="w-20 h-32 rounded-xl hover:cursor-pointer hover:opacity-75" />
          <img src="/story_img.jpg" className="w-20 h-32 rounded-xl" />
        </div>
      </section>
    </div>
  );
};
